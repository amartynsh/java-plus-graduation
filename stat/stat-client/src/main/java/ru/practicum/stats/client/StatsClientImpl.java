package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;
import ru.practicum.stats.dto.StatsRequestParamsDto;
import ru.practicum.stats.utils.DateTimeUtil;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class StatsClientImpl implements StatClient {
    private final RestTemplate rest;

    @Autowired
    public StatsClientImpl(@Value("${stats-server.url}") String statUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    @Override
    public void hit(HitDto hitDto) {
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(hitDto, defaultHeaders());
        try {
            rest.exchange("/hit", HttpMethod.POST, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            log.error("Hit stats was not successful with code {} and message {}", e.getStatusCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Hit stats was not successful with exception {} and message {}", e.getClass().getName(), e.getMessage(), e);
        }
    }

    @Override
    public List<StatsDto> get(StatsRequestParamsDto statsRequestParamsDto) {
        if (!checkValidRequestParamsDto(statsRequestParamsDto)) {
            log.error("Get stats was not successful because of incorrect parameters {}", statsRequestParamsDto);
            return List.of();
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", statsRequestParamsDto.getStart().format(DateTimeUtil.DATE_TIME_FORMATTER))
                .queryParam("end", statsRequestParamsDto.getEnd().format(DateTimeUtil.DATE_TIME_FORMATTER));

        if (statsRequestParamsDto.getUris() != null && !statsRequestParamsDto.getUris().isEmpty()) {
            uriComponentsBuilder.queryParam("uris", statsRequestParamsDto.getUris());
        }
        if (statsRequestParamsDto.getUnique() != null) {
            uriComponentsBuilder.queryParam("unique", statsRequestParamsDto.getUnique());
        }
        String uri = uriComponentsBuilder.build(false)
                .encode()
                .toUriString();

        HttpEntity<String> requestEntity = new HttpEntity<>(defaultHeaders());
        ResponseEntity<StatsDto[]> statServerResponse;
        try {
            statServerResponse = rest.exchange(uri, HttpMethod.GET, requestEntity, StatsDto[].class);
        } catch (HttpStatusCodeException e) {
            log.error("Get stats was not successful with code {} and message {}", e.getStatusCode(), e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Get stats was not successful with exception {} and message {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
        statServerResponse.getBody();
        return List.of(Objects.requireNonNull(statServerResponse.getBody()));
    }

    private boolean checkValidRequestParamsDto(StatsRequestParamsDto statsRequestParamsDto) {
        if (statsRequestParamsDto.getStart() == null || statsRequestParamsDto.getEnd() == null
                || statsRequestParamsDto.getStart().isAfter(statsRequestParamsDto.getEnd())) {
            return false;
        }

        if (statsRequestParamsDto.getUris() != null && statsRequestParamsDto.getUris().isEmpty()) {
            return false;
        }

        return true;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}