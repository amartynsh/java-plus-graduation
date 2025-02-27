package ru.practicum.clients.location;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;

@FeignClient(name = "location-service")
public interface LocationClient {
    @PostMapping("/locations")
    LocationDto getBy(@RequestBody NewLocationDto newLocationDto);

    @GetMapping("/locations/{locationId}")
    LocationDto getById(@PathVariable Long locationId);
}
