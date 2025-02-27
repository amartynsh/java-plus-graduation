package ru.practicum.event.service;


import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.clients.location.LocationClient;
import ru.practicum.clients.participationrequest.PrivateParticipationRequestClient;
import ru.practicum.clients.user.AdminUserClient;
import ru.practicum.core.error.exception.ConflictDataException;
import ru.practicum.core.error.exception.NotFoundException;
import ru.practicum.core.error.exception.ValidationException;
import ru.practicum.core.util.DateTimeUtil;
import ru.practicum.core.util.PagingUtil;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;
import ru.practicum.dto.participationrequest.ParticipationRequestStatus;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.handler.EventHandler;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.EventToDtoMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;
import ru.practicum.stats.dto.StatsRequestParamsDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoriesRepository categoriesRepository;
    private final EventMapper eventMapper;
    private static final String appNameForStat = "ewm-main-service";
    private final EntityManager entityManager;
    private final StatClient statClient;

    private final AdminUserClient adminUserClient;
    private final LocationClient locationClient;
    private final EventHandler eventHandler;
    private final PrivateParticipationRequestClient privateParticipationRequestClient;


    private Event checkAndGetEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiator(eventId, initiatorId)
                .orElseThrow(() -> new NotFoundException(String.format("On event operations - " +
                        "Event doesn't exist with id %s or not available for User with id %s: ", eventId, initiatorId)));
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        checkEventTime(newEventDto.getEventDate());
        Category category = categoriesRepository.findById(newEventDto.getCategory()).get();
        UserDto user = adminUserClient.getById(userId);
        LocationDto location = getOrCreateLocation(newEventDto.getLocation());
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, category, user.getId(), location.getId()));
        return EventToDtoMapper.eventToFullDto(event, user, location, category);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long id, int from, int size) {
        PageRequest page = PagingUtil.pageOf(from, size).withSort(Sort.by(Sort.Order.desc("eventDate")));
        List<Event> events = eventRepository.findAllByInitiator(id, page);

        List<EventShortDto> eventsDto = eventHandler.getListEventShortDto(events);

        populateWithConfirmedRequests(events, eventsDto);
        populateWithStats(eventsDto);
        return eventsDto;
    }


    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        //Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);
        Event event = eventRepository.findById(eventId).get();
        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of());
        populateWithStats(List.of(eventDto));

        return eventDto;
    }


    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequestDto eventUpdateDto) {
        Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);

        if (event.getState() == EventStates.PUBLISHED)
            throw new ConflictDataException(
                    String.format("On Event private update - " +
                            "Event with id %s can't be changed because it is published.", event.getId()));
        checkEventTime(eventUpdateDto.getEventDate());

        eventMapper.update(event, eventUpdateDto, getOrCreateLocation(eventUpdateDto.getLocation()).getId());
        if (eventUpdateDto.getStateAction() != null) {
            setStateToEvent(eventUpdateDto, event);
        }
        event.setId(eventId);

        event = eventRepository.save(event);

        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        populateWithStats(List.of(eventDto));

        return eventDto;
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("On Event admin update - Event doesn't exist with id: " + eventId));
        log.info("этап 1 пройден");
        Category category = null;
        if (updateEventAdminRequestDto.getCategory() != null)
            category = categoriesRepository.findById(updateEventAdminRequestDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("On Event admin update - Category doesn't exist with id: " +
                            updateEventAdminRequestDto.getCategory()));
        log.info("этап 2 пройден");
        LocationDto locationDto = locationClient.getById(event.getLocation());
        log.info("этап 2.1 пройден");
        log.info("Получено locationDto {}", locationDto);

        event = eventMapper.update(event, updateEventAdminRequestDto, category,
                locationDto.getId());
        log.info("этап 2.2 пройден");
        calculateNewEventState(event, updateEventAdminRequestDto.getStateAction());
        log.info("этап 3 пройден");
        event = eventRepository.save(event);
        log.info("этап 4 пройден");
        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        populateWithStats(List.of(eventDto));

        log.info("Event is updated by admin: {}", event);
        return eventDto;
    }

    @Override
    public EventFullDto get(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("On Event public get - Event doesn't exist with id: " + eventId));

        if (event.getState() != EventStates.PUBLISHED)
            throw new NotFoundException("On Event public get - Event isn't published with id: " + eventId);

        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        populateWithStats(List.of(eventDto));

        hitStat(request);
        return eventDto;
    }

    @Override
    public List<EventFullDto> get(EventAdminFilterParamsDto filters, int from, int size) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();

        if (filters.getUsers() != null && !filters.getUsers().isEmpty())
            builder.and(event.initiator.in(filters.getUsers()));

        if (filters.getStates() != null && !filters.getStates().isEmpty())
            builder.and(event.state.in(filters.getStates()));

        if (filters.getCategories() != null && !filters.getCategories().isEmpty())
            builder.and(event.category.id.in(filters.getCategories()));

        if (filters.getRangeStart() != null)
            builder.and(event.eventDate.goe(filters.getRangeStart()));

        if (filters.getRangeEnd() != null)
            builder.and(event.eventDate.loe(filters.getRangeEnd()));

        List<Event> events = eventRepository.findAll(builder,
                PagingUtil.pageOf(from, size).withSort(new QSort(event.createdOn.desc()))).toList();

        List<EventFullDto> eventsDto = eventHandler.getListEventFullDto(events);
        populateWithConfirmedRequests(events, eventsDto);
        populateWithStats(eventsDto);

        return eventsDto;
    }

    @Override
    public List<EventShortDto> get(EventPublicFilterParamsDto filters, int from, int size, HttpServletRequest request) {
        QEvent qEvent = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(qEvent.state.eq(EventStates.PUBLISHED));

        if (filters.getText() != null)
            builder.and(qEvent.annotation.containsIgnoreCase(filters.getText())
                    .or(qEvent.description.containsIgnoreCase(filters.getText())));

        if (filters.getCategories() != null && !filters.getCategories().isEmpty())
            builder.and(qEvent.category.id.in(filters.getCategories()));

        if (filters.getPaid() != null)
            builder.and(qEvent.paid.eq(filters.getPaid()));

        if (filters.getRangeStart() == null && filters.getRangeEnd() == null)
            builder.and(qEvent.eventDate.goe(DateTimeUtil.currentDateTime()));
        else {
            if (filters.getRangeStart() != null)
                builder.and(qEvent.eventDate.goe(filters.getRangeStart()));

            if (filters.getRangeEnd() != null)
                builder.and(qEvent.eventDate.loe(filters.getRangeEnd()));
        }

/*        if (filters.getLon() != null && filters.getLat() != null)
            builder.and(Expressions.booleanTemplate("distance({0}, {1}, {2}, {3}) <= {4}",
                    qEvent.location.lat,
                    qEvent.location.lon,
                    filters.getLat(),
                    filters.getLon(),
                    filters.getRadius()));*/

        PageRequest page = PagingUtil.pageOf(from, size);
        if (filters.getSort() != null && filters.getSort() == EventPublicFilterParamsDto.EventSort.EVENT_DATE)
            page.withSort(new QSort(qEvent.eventDate.desc()));

        List<Event> events = eventRepository.findAll(builder, page).toList();

        List<EventShortDto> eventsDto = eventHandler.getListEventShortDto(events);
        populateWithConfirmedRequests(events, eventsDto, true);
        populateWithStats(eventsDto);

        if (filters.getSort() != null && filters.getSort() == EventPublicFilterParamsDto.EventSort.VIEWS)
            eventsDto.sort(Comparator.comparing(EventShortDto::getViews).reversed());

        hitStat(request);
        return eventsDto;
    }

    @Override
    public List<ParticipationRequestDto> getEventAllParticipationRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Такого события не существует: " + eventId));
        checkEventOwner(event, userId);

        return privateParticipationRequestClient.getParticipationRequestsBy(eventId,
                ParticipationRequestStatus.PENDING.toString());

    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto changeEventState(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        log.info("Change event state by user: {} and event id: {}", userId, eventId);

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Такого события не существует: " + eventId));
        adminUserClient.getById(userId);
        checkEventOwner(event, userId);
        int participantsLimit = event.getParticipantLimit();

        // "если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется"
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.info("Заявки подтверждать не требуется");
            return null;
        }

        return privateParticipationRequestClient.changeEventState(userId, eventId, participantsLimit, statusUpdateRequest);
    }

    private LocationDto getOrCreateLocation(NewLocationDto newLocationDto) {
        return locationClient.getBy(newLocationDto);
    }

    private void calculateNewEventState(Event event, EventStateActionAdmin stateAction) {
        if (stateAction == EventStateActionAdmin.PUBLISH_EVENT) {
            if (event.getState() != EventStates.PENDING) {
                throw new ConflictDataException(
                        String.format("On Event admin update - " +
                                        "Event with id %s can't be published from the state %s: ",
                                event.getId(), event.getState()));
            }

            LocalDateTime currentDateTime = DateTimeUtil.currentDateTime();
            if (currentDateTime.plusHours(1).isAfter(event.getEventDate()))
                throw new ConflictDataException(
                        String.format("On Event admin update - " +
                                        "Event with id %s can't be published because the event date is to close %s: ",
                                event.getId(), event.getEventDate()));

            event.setPublishedOn(currentDateTime);
            event.setState(EventStates.PUBLISHED);
        } else if (stateAction == EventStateActionAdmin.REJECT_EVENT) {
            if (event.getState() == EventStates.PUBLISHED) {
                throw new ConflictDataException(
                        String.format("On Event admin update - " +
                                        "Event with id %s can't be canceled because it is already published: ",
                                event.getState()));
            }

            event.setState(EventStates.CANCELED);
        }
    }

    private void setStateToEvent(UpdateEventUserRequestDto eventUpdateDto, Event event) {
        if (eventUpdateDto.getStateAction().toString()
                .equalsIgnoreCase(EventStateActionPrivate.CANCEL_REVIEW.toString())) {
            event.setState(EventStates.CANCELED);
        } else if (eventUpdateDto.getStateAction().toString()
                .equalsIgnoreCase(EventStateActionPrivate.SEND_TO_REVIEW.toString())) {
            event.setState(EventStates.PENDING);
        }
    }

    private void checkEventTime(LocalDateTime eventDate) {
        if (eventDate == null) return;
        log.info("Проверяем дату события на корректность: {}", eventDate);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime correctEventTime = eventDate.plusHours(2);
        if (correctEventTime.isBefore(now)) {
            log.info("дата не корректна");
            throw new ValidationException("Дата события должна быть +2 часа вперед");
        }
    }

    private void checkEventOwner(Event event, Long userId) {
        if (!Objects.equals(event.getInitiator(), userId)) {
            throw new ValidationException("Событие создал другой пользователь");
        }
    }

    private void populateWithConfirmedRequests(List<Event> events, List<? extends EventShortDto> eventsDto) {
        populateWithConfirmedRequests(events, eventsDto, null);
    }

    private void populateWithConfirmedRequests(List<Event> events, List<? extends EventShortDto> eventsDto,
                                               Boolean filterOnlyAvailable) {


        List<ParticipationRequestDto> confirmedRequests = privateParticipationRequestClient
                .findConfirmedRequestsByEventIds(events.stream().map(Event::getId).toList());


        Map<Long, Long> confirmedRequestsCountByEventId = confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        ParticipationRequestDto::getEvent,
                        Collectors.counting()
                ));

        // Обрабатываем каждое событие в eventsDto


        for (var eventDto : eventsDto) {
            Long eventId = eventDto.getId();
            Long confirmedRequestsCount = confirmedRequestsCountByEventId.getOrDefault(eventId, 0L);

            // Если включена фильтрация по доступности
            if (filterOnlyAvailable != null && filterOnlyAvailable) {
                // Загружаем событие, чтобы получить participantLimit
                Event event = eventRepository.findById(eventId).orElse(null);
                if (event != null) {
                    int participantLimit = event.getParticipantLimit();
                    // Проверяем доступность
                    if (participantLimit != 0 && confirmedRequestsCount >= participantLimit) {
                        confirmedRequestsCount = -1L; // Событие недоступно
                    }
                }
            }

            // Устанавливаем количество подтвержденных запросов
            eventDto.setConfirmedRequests(confirmedRequestsCount);
        }

        // Если включена фильтрация по доступности, удаляем недоступные события
        if (filterOnlyAvailable != null && filterOnlyAvailable) {
            eventsDto.removeIf(eventDto -> eventDto.getConfirmedRequests() < 0);
        }
    }

    private void populateWithStats(List<? extends EventShortDto> eventsDto) {
        if (eventsDto.isEmpty()) return;

        Map<String, EventShortDto> uris = eventsDto.stream()
                .collect(Collectors.toMap(e -> String.format("/events/%s", e.getId()), e -> e));

        LocalDateTime currentDateTime = DateTimeUtil.currentDateTime();
        List<StatsDto> stats = statClient.get(StatsRequestParamsDto.builder()
                .start(currentDateTime.minusDays(1))
                .end(currentDateTime)
                .uris(uris.keySet().stream().toList())
                .unique(true)
                .build());

        stats.forEach(stat -> Optional.ofNullable(uris.get(stat.getUri()))
                .ifPresent(e -> e.setViews(stat.getHits())));
    }

    private void hitStat(HttpServletRequest request) {
        statClient.hit(HitDto.builder()
                .app(appNameForStat)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(DateTimeUtil.currentDateTime())
                .build());
    }
}
