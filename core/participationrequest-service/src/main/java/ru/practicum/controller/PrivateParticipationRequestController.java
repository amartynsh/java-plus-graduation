package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.clients.participationrequest.PrivateParticipationRequestClient;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateParticipationRequestController implements PrivateParticipationRequestClient {
    private final ParticipationRequestService participationRequestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info(" @PostMapping /{userId}/requests");
        return participationRequestService.create(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        log.info(" @GetMapping /{userId}/requests}");
        return participationRequestService.get(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info(" @PatchMapping /{userId}/requests/{requestId}/cancel");
        return participationRequestService.cancel(userId, requestId);
    }

    @GetMapping("/participationrequest")
    public List<ParticipationRequestDto> getParticipationRequestsBy(@RequestParam(name = "eventId") Long eventId,
                                                                    @RequestParam(name = "status") String status) {
        log.info(" @GetMapping /participationrequest/ with parameters {eventId}/{status}");
        return participationRequestService.getEventAllParticipationRequests(eventId, status);
    }

    @GetMapping("/participationrequest/{requestId}")
    public ParticipationRequestDto getParticipationRequestsByRequest(@PathVariable Long requestId) {
        log.info(" @GetMapping /participationrequest/{requestId}");
        return participationRequestService.getParticipationRequestsByRequest(requestId);
    }

    @PatchMapping("/requests/{requestId}")
    public void updateStatus(@PathVariable Long requestId, @RequestParam String status) {
        log.info(" @PatchMapping /requests/{requestId} with parameters {status}");
        participationRequestService.updateStatus(requestId, status);
    }


    @PostMapping("/participationrequest")
    public EventRequestStatusUpdateResultDto changeEventState(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "eventId") Long eventId,
                                                              @RequestParam(name = "participantsLimit") int participantsLimit,
                                                              @RequestBody EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        log.info(" @PostMapping /participationrequest with parameters {userId}/{eventId}/{participantsLimit}");
        return participationRequestService.changeEventState(userId, eventId, statusUpdateRequest, participantsLimit);
    }

    @PostMapping("/participationrequest/confirmed")
    public List<ParticipationRequestDto> findConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds) {
        log.info(" @PostMapping /participationrequest/confirmed");
        return participationRequestService.confirmedRequestsByEventList(eventIds);
    }
}