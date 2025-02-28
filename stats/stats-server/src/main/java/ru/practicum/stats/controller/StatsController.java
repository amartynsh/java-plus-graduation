package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;
import ru.practicum.stats.service.StatsService;
import ru.practicum.stats.utils.DateTimeUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody @Valid HitDto hitDto) {
        statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam String start,
                                   @RequestParam String end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(defaultValue = "false") boolean unique) {
        LocalDateTime startTime = LocalDateTime.parse(
                URLDecoder.decode(start, StandardCharsets.UTF_8),
                DateTimeUtil.DATE_TIME_FORMATTER
        );
        LocalDateTime endTime = LocalDateTime.parse(
                URLDecoder.decode(end, StandardCharsets.UTF_8),
                DateTimeUtil.DATE_TIME_FORMATTER
        );
        return statsService.getStats(startTime, endTime, uris, unique);
    }
}
