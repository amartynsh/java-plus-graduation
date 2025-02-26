package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationAdminRequestDto;
import ru.practicum.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toLocation(NewLocationDto newLocationDto);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Location update(@MappingTarget Location location, UpdateLocationAdminRequestDto updateLocationAdminRequestDto);
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LocationDto toLocationDto(NewLocationDto location);
}