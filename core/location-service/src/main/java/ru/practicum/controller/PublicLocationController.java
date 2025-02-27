package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.service.LocationService;

import java.util.List;

@RestController
@RequestMapping(path = "/locations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicLocationController  {
    private final LocationService locationService;

    @GetMapping
    public List<LocationDto> getLocations(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /locations?from={}&size={}", from, size);
        return locationService.getLocations(from, size);
    }

    @GetMapping("/{locationId}")
    public LocationDto getById(@PathVariable Long locationId) {
        log.info("GET /locations/{}", locationId);
        return locationService.getById(locationId);
    }

    @PostMapping
    public LocationDto getBy(@RequestBody NewLocationDto newLocationDto) {
        return locationService.findLocationBy(newLocationDto);
    }
}
