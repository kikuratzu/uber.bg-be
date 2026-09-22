package com.uber.bg.uber.bg.Entities;

import jakarta.persistence.*;
import lombok.*;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cars")
public class Car extends BaseEntity {

    @Column(name = "brand", nullable = false)
    private String brand;
    @Column(name = "model", nullable = false)
    private String model;
    @Column(name = "plate_number", nullable = false, unique = true)
    private String plateNumber;

    @Column(name = "car_photo", columnDefinition = "TEXT")
    private String carPhoto;

    @ManyToMany(mappedBy = "vehicles")
    private Set<User> drivers = new HashSet<>();

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
