package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findLocationByLatAndLon(Double lat, Double lon);

    @Query("select l from Location l where distance(l.lat, l.lon, :latitude, :longitude) <= :radius")
    List<Location> findByRadius(Double lat, Double lon, Double radius);
}
