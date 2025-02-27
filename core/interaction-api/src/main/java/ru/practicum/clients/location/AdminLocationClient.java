/*
package ru.practicum.clients.location;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationAdminRequestDto;

@FeignClient(name = "admin-location-service")
public interface AdminLocationClient {
    @PostMapping
    public LocationDto addLocation(@RequestBody @Valid NewLocationDto newLocationDto);

    @PatchMapping("/{locationId}")
    public LocationDto updateLocation(@PathVariable(name = "locationId") Long locationId,
                                      @RequestBody @Valid UpdateLocationAdminRequestDto updateLocationAdminRequestDto);

    @DeleteMapping("/{locationId}")
    public void delete(@PathVariable(name = "locationId") Long locationId);

}
*/
