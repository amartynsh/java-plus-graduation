package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.error.exception.NotFoundException;
import ru.practicum.core.util.PagingUtil;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationAdminRequestDto;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Location;
import ru.practicum.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    LocationRepository locationRepository;
    LocationMapper locationMapper;

    @Override
    public List<LocationDto> getLocations(Integer from, Integer size) {
        log.info("start getLocations by from {} size {}", from, size);
        return locationRepository.findAll(PagingUtil.pageOf(from, size)).stream()
                .map(locationMapper::toDto).toList();
    }

    @Override
    public LocationDto getById(Long locationId) {
        log.info("getById params: id = {}", locationId);
        Location location = locationRepository.findById(locationId).orElseThrow(() -> new NotFoundException(
                String.format("Локация с ид %s не найдена", locationId))
        );
        log.info("getById result location = {}", location);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDto addLocation(NewLocationDto newLocationDto) {
        Location location = locationRepository.save(locationMapper.toLocation(newLocationDto));
        log.info("Location is created: {}", location);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDto updateLocation(Long locationId, UpdateLocationAdminRequestDto updateLocationAdminRequestDto) {
        log.info("start updateLocation");
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location with id " + locationId + " not found"));
        location = locationRepository.save(locationMapper.update(location, updateLocationAdminRequestDto));
        log.info("Location is updated: {}", location);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public void delete(Long locationId) {
        locationRepository.deleteById(locationId);
        log.info("Location deleted with id: {}", locationId);
    }
    @Transactional
    @Override
    public LocationDto findLocationBy(NewLocationDto newLocationDto) {
        log.info("start findLocationById");

        Optional<Location> location = locationRepository.findByLatAndLon(newLocationDto.getLat(), newLocationDto.getLon());
        Location newLocation;
        if (!location.isPresent()) {
            log.info("местоположение не найдено, сохраняем");
            newLocation = locationRepository.save(locationMapper.toLocation(newLocationDto));
        } else {
            newLocation = location.get();
        }
        log.info("findLocationBy result location = {}", newLocation);
        return locationMapper.toDto(newLocation);
    }


    // return locationMapper.toLocationDto(newLocationDto) == null ? null : locationRepository.findByLatAndLon(newLocationDto.getLat(), newLocationDto.getLon())
    //       locationMapper.toDto(locationRepository.save(locationMapper.toLocation(newLocationDto))));
}
