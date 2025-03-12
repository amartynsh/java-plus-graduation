package ru.practicum.clients.participationrequest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "participation-service")
public interface PrivateParticipationRequestClient {

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