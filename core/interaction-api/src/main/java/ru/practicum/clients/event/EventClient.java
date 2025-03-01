package ru.practicum.clients.event;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {

    //Получение событий, добавленных текущим пользователем
    @GetMapping(path = "/users/{userId}/events")
    List<EventShortDto> getEvent(@PathVariable("userId") Long userId,
                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                 @Positive @RequestParam(defaultValue = "10") int size);

    //Получение полной информации о событии добавленном текущим пользователем
    @GetMapping(path = "/users/{userId}/events/{eventId}")
    EventFullDto getEvent(@PathVariable("userId") Long userId, @PathVariable("eventId") Long eventId);


    @GetMapping("/admin/events/locations")
    Boolean get(@RequestParam Long locationId);
}
