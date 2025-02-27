package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import ru.practicum.model.ParticipationRequest;
import ru.practicum.dto.participationrequest.ParticipationRequestStatus;


import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long>,
        QuerydslPredicateExecutor<ParticipationRequest> {

    @Query("Select pr from ParticipationRequest as pr where pr.requester = :requester and pr.event <> :requester")
    List<ParticipationRequest> findByRequester(@Param("requester") Long requester);

    boolean existsByRequesterAndEvent(Long requester, Long event);

    long countByEventAndStatusIn(Long event, List<ParticipationRequestStatus> status);

    List<ParticipationRequest> findAllByEventAndStatus(Long eventId, ParticipationRequestStatus status);

    // Optional<ParticipationRequest> findById(Long requestId);
    @Query("SELECT pr FROM ParticipationRequest pr " +
            "WHERE pr.status = :status AND pr.event IN :events")
    List<ParticipationRequest> findConfirmedRequestsByEventIds(@Param("status") ParticipationRequestStatus status,
                                                               @Param("events") List<Long> events);
}