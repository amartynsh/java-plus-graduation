package ru.practicum.event.handler;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.clients.location.PublicLocationClient;
import ru.practicum.clients.user.AdminUserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class EventHandler {

    private final AdminUserClient adminUserClient;
    private final PublicLocationClient publicLocationClient;
    private final EventMapper eventMapper;

    public EventHandler(AdminUserClient adminUserClient, PublicLocationClient publicLocationClient, EventMapper eventMapper) {
        this.adminUserClient = adminUserClient;
        this.publicLocationClient = publicLocationClient;
        this.eventMapper = eventMapper;
    }

    public EventFullDto getEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        UserShortDto userShortDto = eventMapper.toUserShortDto(adminUserClient.getById(event.getInitiator()));
        LocationDto locationDto = publicLocationClient.getById(event.getLocation());
        return eventMapper.toFullDto(event, locationDto, userShortDto);
    }


    public List<EventFullDto> getListEventFullDto(List<Event> events) {

        List<UserShortDto> userShortDtos = eventMapper.toUserShortDtos(adminUserClient
                .getUsers(events.stream().map(Event::getInitiator)
                        .toList(), 0, 10));

        EventFullDto eventFullDto = new EventFullDto();
        List<EventFullDto> eventsFullDtos = new ArrayList<>();

        for (Event event : events) {
            UserShortDto user = null;
            for (UserShortDto userShortDto : userShortDtos) {
                if (Objects.equals(userShortDto.getId(), event.getInitiator())) {
                    user = userShortDto;
                }
            }
            EventShortDto eventShortDto = eventMapper.toShortDto(event, user);
            eventsFullDtos.add(eventFullDto);
        }
        return eventsFullDtos;
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
        LocationDto locationDto = publicLocationClient.getById(event.getLocation());
        return eventMapper.toFullDto(event, locationDto, userShortDto);
    }
}
