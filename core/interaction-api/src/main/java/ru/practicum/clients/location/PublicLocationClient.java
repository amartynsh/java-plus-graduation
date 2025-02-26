package ru.practicum.clients.location;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;

import java.util.List;

@FeignClient(name = "public-location-service1")
public interface PublicLocationClient {
    @GetMapping
    List<LocationDto> getLocations(@RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/{locationId}")
    LocationDto getById(@PathVariable Long locationId);

    @PostMapping
    LocationDto getBy(@RequestBody NewLocationDto newLocationDto);

}
