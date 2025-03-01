package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.event.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "id", source = "event.id")
    @Named(value = "EventShortDto")
    @Mapping(target = "initiator", source = "userShortDto")
    EventShortDto toShortDto(Event event, UserShortDto userShortDto);

   /* @IterableMapping(qualifiedByName = "EventShortDto")
    List<EventShortDto> toShortDto(Iterable<Event> event);*/

    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "location", source = "locationDto")
    @Mapping(target = "initiator", source = "userShortDto")
    @Mapping(target = "id", source = "event.id")
    EventFullDto toFullDto(Event event, LocationDto locationDto, UserShortDto userShortDto);

    //List<EventFullDto> toFullDto(Iterable<Event> event);

    //List<EventShortDto> toEventShortDtoList(Iterable<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "userFromRequest")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "participantLimit", defaultValue = "0")
    @Mapping(target = "paid", defaultValue = "false")
    @Mapping(target = "requestModeration", defaultValue = "true")
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Event toEvent(NewEventDto newEventDto, Category category, Long userFromRequest, Long location);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "location", source = "location")
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Event update(@MappingTarget Event event, UpdateEventUserRequestDto eventUpdateDto, Long location);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "location", source = "location")
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Event update(@MappingTarget Event event, UpdateEventAdminRequestDto eventUpdateDto, Category category, Long location);

    UserShortDto toUserShortDto(UserDto userDto);

    List<UserShortDto> toUserShortDtos(Iterable<UserDto> userDto);
}