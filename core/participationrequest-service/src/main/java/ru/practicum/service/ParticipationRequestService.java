package ru.practicum.service;


import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventAllParticipationRequests(Long eventId, String status);

    ParticipationRequestDto getParticipationRequestsByRequest(Long requestId);

    void updateStatus(Long requestId, String status);

    EventRequestStatusUpdateResultDto changeEventState(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequestDto statusUpdateRequest,
                                                       int participantsLimit);

    List<ParticipationRequestDto> confirmedRequestsByEventList(List<Long> events);
}