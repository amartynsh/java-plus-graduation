/*
package ru.practicum.clients.event;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventAdminFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequestDto;

import java.util.List;

@FeignClient(name = "event-service-admin-event")

public interface AdminEventClient {
    @PatchMapping("/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventAdminRequestDto updateEventAdminRequestDto);

    @GetMapping
    List<EventFullDto> get(@Valid EventAdminFilterParamsDto filters,
                           @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                           @Positive @RequestParam(defaultValue = "10") int size);
}

*/
