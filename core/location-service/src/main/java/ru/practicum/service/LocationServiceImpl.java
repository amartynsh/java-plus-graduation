package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.clients.event.EventClient;
import ru.practicum.core.error.exception.ConflictDataException;
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
    EventClient eventClient;

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
        Location location = locationMapper.toLocation(newLocationDto);
        Location locationSaved = locationRepository.save(locationMapper.toLocation(newLocationDto));
        log.info("Location is created: {}", location);
        return locationMapper.toDto(locationSaved);
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

        if (eventClient.get(locationId)) {
            throw new ConflictDataException("Есть связанные с этим местоположением события");
        }
        locationRepository.deleteById(locationId);
        log.info("Location deleted with id: {}", locationId);
    }

    @Transactional
    @Override
    public LocationDto findLocationBy(NewLocationDto newLocationDto) {
        log.info("start findLocationBy newLocationDto {}", newLocationDto);
        if (newLocationDto.getLat() == 0 && newLocationDto.getLon() == 0) {
            newLocationDto.setLat(0.0);
            newLocationDto.setLon(0.0);
        }

        if (newLocationDto == null) {
            log.info("newLocationDto is null, условие сработало");
            return null;
        }
        Optional<Location> location = locationRepository.findLocationByLatAndLon(newLocationDto.getLat(), newLocationDto.getLon());
        Location newLocation;


        if (location.isEmpty()) {
            log.info("местоположение не найдено, сохраняем");
            newLocation = locationRepository.save(locationMapper.toLocation(newLocationDto));
        } else {
            log.info("местоположение найдено, достаем из БД id={}", location.get().getId());
            newLocation = location.get();
        }
        log.info("findLocationBy result location = {}", newLocation);
        return locationMapper.toDto(newLocation);
    }

    @Override
    public List<LocationDto> getLocationByRadius(Double lat, Double lon, Double radius) {
        log.info("Поиск местоположений по радиусу и координатам, lat, lon, radius");
        List<LocationDto> locationDtos = locationRepository.findByRadius(lat, lon, radius)
                .stream()
                .map(locationMapper::toDto)
                .toList();
        log.info("Нашли список локаций: ", locationDtos);
        return locationDtos;
    }
}
