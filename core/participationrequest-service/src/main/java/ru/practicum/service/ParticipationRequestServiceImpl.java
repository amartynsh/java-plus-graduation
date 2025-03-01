package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.clients.event.EventClient;
import ru.practicum.clients.user.AdminUserClient;
import ru.practicum.core.error.exception.ConflictDataException;
import ru.practicum.core.error.exception.NotFoundException;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.event.EventStates;
import ru.practicum.dto.participationrequest.ParticipationRequestDto;
import ru.practicum.dto.participationrequest.ParticipationRequestStatus;
import ru.practicum.dto.user.UserDto;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;
    private final AdminUserClient userClient;
    private final EventClient eventClient;

    private UserDto checkAndGetUserById(Long userId) {
        return userClient.getById(userId);
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        UserDto requester = checkAndGetUserById(userId);
        EventFullDto event = eventClient.getEvent(userId, eventId);

        if (!event.getState().equals(EventStates.PUBLISHED))
            throw new ConflictDataException("On part. request create - " +
                    "Event isn't published with id: " + eventId);


        if (event.getInitiator().getId().equals(userId))
            throw new ConflictDataException(
                    String.format("On part. request create - " +
                            "Event with id %s has Requester with id %s as an initiator: ", eventId, userId));

        if (participationRequestRepository.existsByRequesterAndEvent(requester.getId(), event.getId()))
            throw new ConflictDataException(
                    String.format("On part. request create - " +
                            "Request by Requester with id %s and Event with id %s already exists: ", eventId, userId));

        if (event.getParticipantLimit() != 0) {
            long requestsCount = participationRequestRepository.countByEventAndStatusIn(event.getId(),
                    List.of(ParticipationRequestStatus.CONFIRMED));
            if (requestsCount >= event.getParticipantLimit())
                throw new ConflictDataException(
                        String.format("On part. request create - " +
                                "Event with id %s reached the limit of participants and User with id %s can't apply: ", eventId, userId));
        }

        ParticipationRequest createdParticipationRequest = participationRequestRepository.save(
                ParticipationRequest.builder()
                        .requester(requester.getId())
                        .event(event.getId())
                        .status(event.getParticipantLimit() != 0 && event.getRequestModeration() ?
                                ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED)
                        .build()
        );
        log.info("Participation request is created: {}", createdParticipationRequest);
        return participationRequestMapper.toDto(createdParticipationRequest);
    }

    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        UserDto requester = checkAndGetUserById(userId);

        List<ParticipationRequest> participationRequests = participationRequestRepository.findByRequester(requester.getId());
        log.trace("Participation requests are requested by user with id {}", userId);
        return participationRequestMapper.toDto(participationRequests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        checkAndGetUserById(userId);

        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("On part. request cancel - Request doesn't exist with id: " + requestId));

        if (!participationRequest.getRequester().equals(userId))
            throw new NotFoundException(String.format("On part. request cancel - " +
                    "Request with id %s can't be canceled by not owner with id %s: ", requestId, userId));

        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);
        participationRequest = participationRequestRepository.save(participationRequest);
        log.info("Participation request is canceled: {}", participationRequest);
        return participationRequestMapper.toDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventAllParticipationRequests(Long eventId, String status) {
        return participationRequestRepository.findAllByEventAndStatus(eventId,
                        ParticipationRequestStatus.valueOf(status.toUpperCase()))
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto getParticipationRequestsByRequest(Long requestId) {
        log.info("Начал работать метод getParticipationRequestsByRequest, на вход пришло requestId{}", requestId);
        ParticipationRequestDto requestDto = participationRequestMapper.toDto(participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса нет")));
        log.info(" Нашли в репозитории {}", requestDto);
        return requestDto;
    }

    @Override
    public void updateStatus(Long requestId, String status) {
        ParticipationRequest request = participationRequestRepository.findById(requestId).get();
        request.setStatus(ParticipationRequestStatus.valueOf(status.toUpperCase()));

    }


    /// //!! Новый метод  - перенёс из эвента

    @Transactional
    @Override
    public EventRequestStatusUpdateResultDto changeEventState(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequestDto statusUpdateRequest,
                                                              int participantsLimit) {
        log.info("Change event state by user: {} and event id: {}", userId, eventId);


        List<ParticipationRequest> confirmedRequests = participationRequestRepository.findAllByEventAndStatus(eventId,
                ParticipationRequestStatus.CONFIRMED);
        List<ParticipationRequest> requestToChangeStatus = statusUpdateRequest.getRequestIds()
                .stream()
                .map(participationRequestRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        //Не очень понял, как обрабатывать это условие:
        // "если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется"

        log.info("Заявки:  Лимит: {}, а заявок {}, разница между ними: {}", participantsLimit,
                statusUpdateRequest.getRequestIds().size(), (participantsLimit
                        - statusUpdateRequest.getRequestIds().size()));

        if (statusUpdateRequest.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
            log.info("меняем статус заявок для статуса: {}", ParticipationRequestStatus.CONFIRMED);
            if ((participantsLimit - (confirmedRequests.size()) - statusUpdateRequest.getRequestIds().size()) >= 0) {
                for (ParticipationRequest request : requestToChangeStatus) {
                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    participationRequestRepository.save(request);
                }
                return new EventRequestStatusUpdateResultDto(requestToChangeStatus
                        .stream().map(participationRequestMapper::toDto)
                        .toList(), null);
            } else {
                throw new ConflictDataException("слишком много участников. Лимит: " + participantsLimit +
                        ", уже подтвержденных заявок: " + confirmedRequests.size() + ", а заявок на одобрение: " +
                        statusUpdateRequest.getRequestIds().size() +
                        ". Разница между ними: " + (participantsLimit - confirmedRequests.size() -
                        statusUpdateRequest.getRequestIds().size()));
            }
        } else if (statusUpdateRequest.getStatus().equals(ParticipationRequestStatus.REJECTED)) {
            log.info("меняем статус заявок для статуса: {}", ParticipationRequestStatus.REJECTED);
            for (ParticipationRequest request : requestToChangeStatus) {
                if (request.getStatus() == ParticipationRequestStatus.CONFIRMED) {
                    throw new ConflictDataException("Заявка" + request.getStatus() + "уже подтверждена.");
                }
                request.setStatus(ParticipationRequestStatus.REJECTED);
                participationRequestRepository.save(request);
            }
            return new EventRequestStatusUpdateResultDto(null, requestToChangeStatus
                    .stream().map(participationRequestMapper::toDto)
                    .toList());
        }
        return null;
    }

    @Override
    public List<ParticipationRequestDto> confirmedRequestsByEventList(List<Long> events) {
        // Загружаем все подтвержденные запросы для указанных событий
        List<ParticipationRequest> confirmedRequests =
                participationRequestRepository.findConfirmedRequestsByEventIds(ParticipationRequestStatus.CONFIRMED, events);
        return confirmedRequests.stream().map(participationRequestMapper::toDto).toList();

    }

    /*public List<Long> checkAvialibleForRegistrationEvents(List<Long> events) {

    }*/

}