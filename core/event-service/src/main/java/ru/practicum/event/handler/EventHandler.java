package ru.practicum.event.handler;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.clients.location.LocationClient;
import ru.practicum.clients.user.AdminUserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class EventHandler {

    private final AdminUserClient adminUserClient;
    private final LocationClient locationClient;
    private final EventMapper eventMapper;
    private final CategoriesRepository categoriesRepository;

    public EventFullDto getEventFullDto(Event event) {
        log.info("EventHandler.getEventFullDto начал обрабатывать event= {} в ", event.getId());
        UserShortDto userShortDto = eventMapper.toUserShortDto(adminUserClient.getById(event.getInitiator()));
        log.info("смапили пользователя {}", userShortDto.toString());
        LocationDto locationDto = locationClient.getById(event.getLocation());
        log.info("Получили местоположение {}", locationDto.toString());
        EventFullDto eventFullDto = eventMapper.toFullDto(event, locationDto, userShortDto);
        log.info("смапили все вместе в eventFullDto = {}", eventFullDto.toString());
        return eventFullDto;
    }


    public List<EventFullDto> getListEventFullDto(List<Event> events) {

        //Получаем пользователя
        List<EventFullDto> eventFullDtos = new ArrayList<>();

        //Получаем список пользователей
        Map<Long, UserShortDto> userShortDtos = adminUserClient
                .getUsers(events
                        .stream()
                        .map(Event::getInitiator)
                        .toList(), 0, 10)
                .stream()
                .collect(Collectors.toMap(
                        userDto -> userDto.getId(),  // Ключ - ID пользователя
                        eventMapper::toUserShortDto          // Значение - сам UserDto
                ));

        log.info("Получили список пользователей : {}", userShortDtos.keySet());
        Map<Long, LocationDto> locationMap = events
                .stream()
                .map(Event::getLocation)
                .distinct()  // Убираем дубликаты, если они могут быть
                .map(locationClient::getById)
                .collect(Collectors.toMap(
                        LocationDto::getId,  // Ключ - ID локации
                        location -> location  // Значение - сама LocationDto
                ));
        log.info("Получили список LocationDto : {}", locationMap.keySet());


        for (Event event : events) {
            eventFullDtos.add(eventMapper.toFullDto(event,
                    locationMap.get(event.getLocation()),
                    userShortDtos.get(event.getInitiator()))
            );
        }

        return eventFullDtos;
    }

    public List<EventShortDto> getListEventShortDto(List<Event> events) {
        List<EventShortDto> eventsDto = new ArrayList<>();
        List<Long> userIds = events.stream()
                .map(Event::getInitiator)
                .toList();
        List<UserShortDto> userShortDtos = adminUserClient.getUsers(userIds, 0, 10)
                .stream()
                .map(eventMapper::toUserShortDto).toList();


        for (Event event : events) {
            UserShortDto user = null;
            for (UserShortDto userShortDto : userShortDtos) {
                if (Objects.equals(userShortDto.getId(), event.getInitiator())) {
                    user = userShortDto;
                }

            }
            EventShortDto eventShortDto = eventMapper.toShortDto(event, user);
            eventsDto.add(eventShortDto);


        }
        return eventsDto;
    }

    EventShortDto getEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventFullDto();
        UserShortDto userShortDto = eventMapper.toUserShortDto(adminUserClient.getById(event.getInitiator()));
        LocationDto locationDto = locationClient.getById(event.getLocation());
        return eventMapper.toFullDto(event, locationDto, userShortDto);
    }
}
