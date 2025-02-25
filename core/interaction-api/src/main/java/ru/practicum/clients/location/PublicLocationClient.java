package ru.practicum.clients.location;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.location.LocationDto;

import java.util.List;

@FeignClient(name = "public-location-service1")
public interface PublicLocationClient {
    @GetMapping
    public List<LocationDto> getLocations(@RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size);

    @GetMapping("/{locationId}")
    public LocationDto getById(@PathVariable Long locationId);
}
