package ru.practicum.clients.participationrequest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "participation-service")
public interface PrivateParticipationRequestClient {

    /*
        @ResponseStatus(HttpStatus.CREATED)
        @PostMapping("/{userId}/requests")
        ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId);

        @GetMapping("/{userId}/requests")
        List<ParticipationRequestDto> get(@PathVariable Long userId);

        @PatchMapping("/{userId}/requests/{requestId}/cancel")
        ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId);



        @GetMapping("/participationrequest/{requestId}")
        ParticipationRequestDto getParticipationRequestsByRequest( @PathVariable Long requestId);

        @PatchMapping("/requests/{requestId}")
        void updateStatus(@PathVariable Long requestId, @RequestParam(name = "status") String status);
    */
    @GetMapping("/users/participationrequest")
    List<ParticipationRequestDto> getParticipationRequestsBy(@RequestParam(name = "eventId") Long eventId,
                                                             @RequestParam(name = "status") String status);

    @PostMapping("/users/participationrequest")
    EventRequestStatusUpdateResultDto changeEventState(@RequestParam(name = "userId") Long userId,
                                                       @RequestParam(name = "eventId") Long eventId,
                                                       @RequestParam(name = "participantsLimit") int participantsLimit,
                                                       @RequestBody EventRequestStatusUpdateRequestDto statusUpdateRequest);

    @PostMapping("/users/participationrequest/confirmed")
    public List<ParticipationRequestDto> findConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds);
}