package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "lat")
    private double lat;
    @Column(name = "lon")
    private double lon;
    private String name;
    private String address;

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
