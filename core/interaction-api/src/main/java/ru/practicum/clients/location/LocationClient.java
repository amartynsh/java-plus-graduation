package ru.practicum.clients.location;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;

import java.util.List;

@FeignClient(name = "location-service")
public interface LocationClient {
    @PostMapping("/locations")
    LocationDto getBy(@RequestBody NewLocationDto newLocationDto);

    @GetMapping("/locations/{locationId}")
    LocationDto getById(@PathVariable Long locationId);

    @GetMapping("/locations/radius")
    List<LocationDto> getByRadius(@RequestParam Double lat, @RequestParam Double lon, @RequestParam Double radius);
}
