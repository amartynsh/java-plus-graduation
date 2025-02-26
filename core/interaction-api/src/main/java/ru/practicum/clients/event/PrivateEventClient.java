package ru.practicum.clients.event;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;


import java.util.List;

@FeignClient(name = "private-event-client")
public interface PrivateEventClient {
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/users/{userId}/events")
    EventFullDto createEvent(@PathVariable("userId") Long userId,
                             @Valid @RequestBody NewEventDto newEventDto);

    //Получение событий, добавленных текущим пользователем
    @GetMapping(path = "/users/{userId}/events")
    List<EventShortDto> getEvent(@PathVariable("userId") Long userId,
                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                 @Positive @RequestParam(defaultValue = "10") int size);

    //Получение полной информации о событии добавленном текущим пользователем
    @GetMapping(path = "/users/{userId}/events/{eventId}")
    EventFullDto getEvent(@PathVariable("userId") Long userId, @PathVariable("eventId") Long eventId);

    //Изменение события добавленного текущим пользователем
    @PatchMapping(path = "/users/{userId}/events/{eventId}")
    EventFullDto updateEvent(@PathVariable("userId") Long userId,
                             @PathVariable("eventId") Long eventId,
                             @Valid @RequestBody UpdateEventUserRequestDto eventUpdateDto);

    //Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping(path = "/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getParticipationRequests(@PathVariable("userId") Long userId,
                                                           @PathVariable("eventId") Long eventId);

    //Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping(path = "/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updatedEventRequestStatus(@PathVariable("userId") Long userId,
                                                                       @PathVariable("eventId") Long eventId,
                                                                       @RequestBody EventRequestStatusUpdateRequestDto request);

}