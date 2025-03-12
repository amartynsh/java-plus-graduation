package ru.practicum.event.mapper;

import ru.practicum.categories.model.Category;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.model.Event;

public class EventToDtoMapper {
    public static EventFullDto eventToFullDto(Event event, UserDto user, LocationDto location, Category category) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .initiator(UserShortDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .build())
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .confirmedRequests(-1L)
                .rating(-1.0)
                .location(location)
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .build();
    }
}