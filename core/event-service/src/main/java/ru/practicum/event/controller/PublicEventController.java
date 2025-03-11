package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicFilterParamsDto;
import ru.practicum.dto.event.EventRecommendationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{id}")
    public EventFullDto get(@PathVariable("id") Long eventId, HttpServletRequest request, @RequestHeader("X-EWM-USER-ID") long userId) {
        return eventService.get(eventId, request, userId);
    }

    @GetMapping
    public List<EventShortDto> get(@Valid EventPublicFilterParamsDto filters,
                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                   @Positive @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        return eventService.get(filters, from, size, request);
    }

    @GetMapping("/recommendations")
    public List<EventRecommendationDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId) {
        return eventService.getRecommendations(userId);
    }
    @PutMapping ("/{eventId}/like")
    public void addLike(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") long userId){
        eventService.addLike(eventId, userId);
    }
}
