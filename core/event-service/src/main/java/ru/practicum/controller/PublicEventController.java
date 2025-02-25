package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventPublicFilterParamsDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{id}")
    public EventFullDto get(@PathVariable("id") Long eventId, HttpServletRequest request) {
        return eventService.get(eventId, request);
    }

    @GetMapping
    public List<EventShortDto> get(@Valid EventPublicFilterParamsDto filters,
                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                   @Positive @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        return eventService.get(filters, from, size, request);
    }
}
