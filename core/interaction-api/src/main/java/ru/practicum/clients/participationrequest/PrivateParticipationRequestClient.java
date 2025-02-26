package ru.practicum.clients.participationrequest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "private-participation-service")
public interface PrivateParticipationRequestClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId);

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId);

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestsBy(@RequestParam(name = "eventId") Long eventId,
                                                                    @RequestParam(name = "status") String status);

    @GetMapping
    public ParticipationRequestDto getParticipationRequestsBy(@RequestParam(name = "requestId") Long requestId);

    @PatchMapping("/requests/{requestId}")
    public void updateStatus(@PathVariable Long requestId, @RequestParam(name = "status") String status);

    @PostMapping
    public EventRequestStatusUpdateResultDto changeEventState (@RequestParam(name = "userId") Long userId,
                                                               @RequestParam(name = "eventId")Long eventId,
                                                               @RequestParam(name = "participantsLimit") int participantsLimit,
                                                               @RequestBody EventRequestStatusUpdateRequestDto statusUpdateRequest) ;
}