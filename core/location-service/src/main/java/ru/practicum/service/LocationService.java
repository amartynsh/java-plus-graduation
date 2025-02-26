package ru.practicum.service;

import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationAdminRequestDto;

import java.util.List;

public interface LocationService {

    List<LocationDto> getLocations(Integer from, Integer size);

    LocationDto getById(Long locationId);

    LocationDto addLocation(NewLocationDto newLocationDto);

    LocationDto updateLocation(Long locationId, UpdateLocationAdminRequestDto updateLocationAdminRequestDto);

    void delete(Long locationId);

    LocationDto findLocationBy(NewLocationDto newLocationDto);
}
