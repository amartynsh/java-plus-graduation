package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.clients.participationrequest.PrivateParticipationRequestClient;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateParticipationRequestController implements PrivateParticipationRequestClient {
    private final ParticipationRequestService participationRequestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        return participationRequestService.create(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return participationRequestService.get(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        return participationRequestService.cancel(userId, requestId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestsBy(@RequestParam(name = "eventId") Long eventId,
                                                                    @RequestParam(name = "status") String status) {
        return participationRequestService.getEventAllParticipationRequests(eventId, status);
    }

    @GetMapping("/participationrequest/{requestId}")
    public ParticipationRequestDto getParticipationRequestsByRequest(@PathVariable Long requestId) {
        return participationRequestService.getParticipationRequestsByRequest(requestId);
    }

    @PatchMapping("/requests/{requestId}")
    public void updateStatus(@PathVariable Long requestId, @RequestParam String status) {
        participationRequestService.updateStatus(requestId, status);
    }


    @PostMapping
    public EventRequestStatusUpdateResultDto changeEventState(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "eventId") Long eventId,
                                                              @RequestParam(name = "participantsLimit") int participantsLimit,
                                                              @RequestBody EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        return participationRequestService.changeEventState(userId, eventId, statusUpdateRequest, participantsLimit);
    }

    @PostMapping("/participations/confirmed")
    public List<ParticipationRequestDto> findConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds) {
        return participationRequestService.confirmedRequestsByEventList(eventIds);
    }
}