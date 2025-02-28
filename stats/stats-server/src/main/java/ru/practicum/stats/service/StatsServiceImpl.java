package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;
import ru.practicum.stats.error.ValidationException;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.mapper.ViewStatsMapper;
import ru.practicum.stats.model.Stats;
import ru.practicum.stats.projection.ViewStatsProjection;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    @Override
    public void saveHit(HitDto hitDto) {
        log.info("Saving hit {}", hitDto);
        Stats stats = endpointHitMapper.toEntity(hitDto);
        Stats savedHit = statsRepository.save(stats);
        log.info("Saved hit {}", savedHit);
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Getting stats for start={}, end={}, uri={}, unique={}", start, end, uris, unique);
        List<ViewStatsProjection> projections;

        if (end.isBefore(start)) {
            throw new ValidationException(String.format("End date %s is before start date %s", end, start));
        }

        if (uris != null && !uris.isEmpty()) {
            if (unique) {
                projections = statsRepository.getUniqueStatsWithHitsAndUris(start, end, uris);
            } else {
                log.info("Сработало условие uniqe=false uris != null && !uris.isEmpty() ");
                log.info("Значение start = {}", start);
                log.info("Значение end = {}", end);
                log.info("Значение uri = {}", uris);
                projections = statsRepository.getStatsWithHitsAndUris(start, end, uris);
                log.info("В Репе нашли {}", projections.toString());
            }
        } else {
            if (unique) {
                projections = statsRepository.getUniqueStatsWithHits(start, end);
            } else {
                projections = statsRepository.getStatsWithHits(start, end);
            }
        }

        return projections.stream()
                .map(viewStatsMapper::toDto)
                .collect(Collectors.toList());
    }
}
