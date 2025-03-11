package ru.practicum.event.service;


import com.querydsl.core.BooleanBuilder;
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
import ru.practicum.grpc.stats.actions.ActionTypeProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoriesRepository categoriesRepository;
    private final EventMapper eventMapper;
    private final AdminUserClient adminUserClient;
    private final LocationClient locationClient;
    private final EventHandler eventHandler;
    private final PrivateParticipationRequestClient privateParticipationRequestClient;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;


    private Event checkAndGetEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiator(eventId, initiatorId)
                .orElseThrow(() -> new NotFoundException(String.format("On event operations - " +
                        "Event doesn't exist with id %s or not available for User with id %s: ", eventId, initiatorId)));
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Adding new event: {}", newEventDto.toString());
        checkEventTime(newEventDto.getEventDate());
        Category category = categoriesRepository.findById(newEventDto.getCategory()).get();
        UserDto user = adminUserClient.getById(userId);
        log.info("Проверяем местоположение из запроса {}", newEventDto.getLocation());
        LocationDto location = getOrCreateLocation(newEventDto.getLocation());
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, category, user.getId(), location.getId()));
        log.info("Сохранили событие: {}", event.toString());
        return EventToDtoMapper.eventToFullDto(event, user, location, category);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long id, int from, int size) {
        PageRequest page = PagingUtil.pageOf(from, size).withSort(Sort.by(Sort.Order.desc("eventDate")));
        List<Event> events = eventRepository.findAllByInitiator(id, page);
        List<EventShortDto> eventsDto = eventHandler.getListEventShortDto(events);
        populateWithConfirmedRequests(events, eventsDto);
        return eventsDto;
    }


    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        //Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);
        Event event = eventRepository.findById(eventId).get();
        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of());
        //
        return eventDto;
    }


    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequestDto eventUpdateDto) {
        log.info("eventServiceImpl.updateEvent, на вход event: {}", eventUpdateDto.toString());
        Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);
        log.info("Из бд достали event: {}", event.toString());

        if (event.getState() == EventStates.PUBLISHED)
            throw new ConflictDataException(
                    String.format("On Event private update - " +
                            "Event with id %s can't be changed because it is published.", event.getId()));
        checkEventTime(eventUpdateDto.getEventDate());
        LocationDto locationDto = locationClient.getById(event.getId());
        if (eventUpdateDto.getLocation() != null) {
            locationDto = getOrCreateLocation(eventUpdateDto.getLocation());
        }
        log.info("Значение newLocation в запросе: {}", locationDto.toString());
        event = eventMapper.update(event, eventUpdateDto, locationDto.getId());
        if (eventUpdateDto.getStateAction() != null) {
            setStateToEvent(eventUpdateDto, event);
        }
        event.setId(eventId);

        event = eventRepository.save(event);

        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        eventDto.setRating(getEventRating(eventId));
        return eventDto;
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        log.info("Началось обновление события, на вход пришло eventId={}, updateEventAdminRequestDto ={}", eventId,
                updateEventAdminRequestDto.toString());
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("On Event admin update - Event doesn't exist with id: " + eventId));
        log.info("Нашли событие event={}", event.toString());
        Category category = null;
        if (updateEventAdminRequestDto.getCategory() != null) {
            category = categoriesRepository.findById(updateEventAdminRequestDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("On Event admin update - Category doesn't exist with id: " +
                            updateEventAdminRequestDto.getCategory()));
            log.info("Нашли категорию category={}", category.toString());
        }


        LocationDto locationDto = locationClient.getById(event.getLocation());
        log.info("Получено местоположение {}", locationDto);

        event = eventMapper.update(event, updateEventAdminRequestDto, category,
                locationDto.getId());
        log.info("eventMapper намапил событие : {}", event.toString());

        calculateNewEventState(event, updateEventAdminRequestDto.getStateAction());

        event = eventRepository.save(event);

        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        log.info("1.Event is updated by admin: {}", event);
        eventDto.setRating(getEventRating(eventId));
        log.info("2.Event is updated by admin: {}", event);
        return eventDto;
    }

    @Override
    public EventFullDto get(Long eventId, HttpServletRequest request, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("On Event public get - Event doesn't exist with id: " + eventId));

        if (event.getState() != EventStates.PUBLISHED)
            throw new NotFoundException("On Event public get - Event isn't published with id: " + eventId);

        EventFullDto eventDto = eventHandler.getEventFullDto(event);
        populateWithConfirmedRequests(List.of(event), List.of(eventDto));
        eventDto.setRating(getEventRating(eventId));
        //long userId, long eventId, ActionTypeProto userAction
        collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        return eventDto;

    }


    @Override
    public List<EventFullDto> get(EventAdminFilterParamsDto filters, int from, int size) {
        log.info("Метод EventServiceImpl.get(filters={}, from={}, size={})", filters, from, size);
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

        log.info("Билдер для поиска = {}", builder.toString());
        List<Event> events = eventRepository.findAll(builder,
                PagingUtil.pageOf(from, size).withSort(new QSort(event.createdOn.desc()))).toList();
        log.info("Нашли в репозитории список событий: {}", events.toString());

        List<EventFullDto> eventsDto = eventHandler.getListEventFullDto(events);
        log.info("В обработчике обработали список событий: {}", eventsDto.toString());
        populateWithConfirmedRequests(events, eventsDto);
        for (EventFullDto eventFullDto : eventsDto) {
            eventFullDto.setRating(getEventRating(eventFullDto.getId()));
        }

        return eventsDto;
    }

    @Override
    public List<EventShortDto> get(EventPublicFilterParamsDto filters, int from, int size, HttpServletRequest request) {

        log.info("Начали поиск сообщения по критериям filters= {}, from={}, size={} ", filters.toString(), from, size);
        List<LocationDto> locationDtos = new ArrayList<>();


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

        if (filters.getLon() != null && filters.getLat() != null) {
            try {
                locationDtos = locationClient.getByRadius(filters.getLat(), filters.getLon(), filters.getRadius());
                builder.and(qEvent.location.in(locationDtos.stream().map(LocationDto::getId).collect(Collectors.toList())));
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        PageRequest page = PagingUtil.pageOf(from, size);
        if (filters.getSort() != null && filters.getSort() == EventPublicFilterParamsDto.EventSort.EVENT_DATE)
            page.withSort(new QSort(qEvent.eventDate.desc()));

        log.info("Билдер для поиска: {}", builder.toString());
        List<Event> events = eventRepository.findAll(builder, page).toList();
        log.info("найно событий: {}", events);
        List<Event> eventsToDelete = new ArrayList<>();

        if (filters.getOnlyAvailable()) {
            log.info("Сработала фильтрация по OnlyAvailable");
            List<ParticipationRequestDto> participationRequestDtos =
                    privateParticipationRequestClient.findConfirmedRequestsByEventIds(events.stream()
                            .map(Event::getId)
                            .toList());
            log.info("Сервис регистрации вернул список: {}", participationRequestDtos.toString());
            for (Event event : events) {
                int confirmedParticipators = 0;
                for (ParticipationRequestDto participationRequestDto : participationRequestDtos) {
                    long participationRequestEventId = participationRequestDto.getEvent();
                    log.info("Сравниваем participationRequestEventId {} == event.getId() {}", participationRequestEventId,
                            event.getId());
                    if (participationRequestEventId == event.getId()) {
                        confirmedParticipators = confirmedParticipators + 1;
                        log.info("Увеличили количество подтвержденных участников {}", confirmedParticipators);
                    }
                }
                if (confirmedParticipators >= event.getParticipantLimit()) {
                    log.info("Улаляем ивент из списка, т.к. он не доступен для регистрации");
                    eventsToDelete.add(event);
                }
                log.info(" Для события {} confirmedParticipators={} и limit ={}", event.getId(), confirmedParticipators, event.getParticipantLimit());
            }
            events = events.stream()
                    .filter(event -> !eventsToDelete.contains(event))
                    .collect(Collectors.toList());
        }


        List<EventShortDto> eventsDto = eventHandler.getListEventShortDto(events);
        for (EventShortDto eventShortDto : eventsDto) {
            eventShortDto.setRating(getEventRating(eventShortDto.getId()));
        }
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
        try {
            return privateParticipationRequestClient.changeEventState(userId, eventId, participantsLimit, statusUpdateRequest);
        } catch (Exception e) {
            throw new ConflictDataException(e.getMessage());
        }
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

    @Override
    public boolean checkPresentEventById(Long locationId) {
        return eventRepository.existsByLocation(locationId);
    }

    private double getEventRating(long eventId) {
        log.info("Получаем рейтинг события");
        Stream<RecommendedEventProto> interactionsCountStream =
                analyzerClient.getInteractionsCount(eventId);
        log.info("Получили поток рекомендаций: {}", interactionsCountStream.toString());
        List<RecommendedEventProto> interactionsCountList = interactionsCountStream.toList();
        // log.info("Получили список рекомендаций: {}", interactionsCountList.toString());
        if (interactionsCountList.isEmpty()) {
            log.info("Поток рекомендаций пустой");
            return 0.0;
        }
        double result = interactionsCountList.stream().findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
        log.info("Рейтинг события: {}", result);
        return result;
    }

    @Override
    public List<EventRecommendationDto> getRecommendations(long userId) {
        log.info("Началось получение рекомендаций для пользователя {}", userId);
        int size = 10;
        List<RecommendedEventProto> recommendedEvents = analyzerClient.getRecommendedEventsForUser(userId, size).toList();
        ;

        List<EventRecommendationDto> eventRecommendationDtoList = null;
        if (recommendedEvents != null) {
            for (RecommendedEventProto recommendedEvent : recommendedEvents) {
                EventRecommendationDto eventRecommendationDto = new EventRecommendationDto();
                eventRecommendationDto.setEventId(recommendedEvent.getEventId());
                eventRecommendationDto.setScore(recommendedEvent.getScore());
                eventRecommendationDtoList.add(eventRecommendationDto);
            }
        }

        return eventRecommendationDtoList;
    }

    @Override
    public void addLike(Long eventId, Long userId) {
        log.info("Добавляем лайк к событию {} от пользователя {}", eventId, userId);
        List<ParticipationRequestDto> requests = privateParticipationRequestClient
                .getParticipationRequestsBy(eventId, String.valueOf(ParticipationRequestStatus.CONFIRMED));
        boolean isRequester = false;
        for (ParticipationRequestDto request : requests) {

            if (request.getRequester() == userId) {
                isRequester = true;
            }
            if (isRequester) {
                collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
            }
        }
        if (!isRequester) {
            throw new ValidationException("Пользователь не был на данном мероприятии");
        }
    }
}
