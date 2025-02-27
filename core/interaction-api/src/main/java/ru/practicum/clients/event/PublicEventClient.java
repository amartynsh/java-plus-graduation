/*
package ru.practicum.clients.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicFilterParamsDto;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service-public-event")
public interface PublicEventClient {

    @GetMapping("/{id}")
    EventFullDto get(@PathVariable("id") Long eventId, HttpServletRequest request);

    @GetMapping
    List<EventShortDto> get(@Valid EventPublicFilterParamsDto filters,
                            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                            @Positive @RequestParam(defaultValue = "10") int size,
                            HttpServletRequest request);
}*/
