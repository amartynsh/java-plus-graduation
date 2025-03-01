package ru.practicum.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    private Long id;
    private double lat;
    private double lon;
    private String name;
    private String address;

    @Override
    public String toString() {
        return "LocationDto{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
