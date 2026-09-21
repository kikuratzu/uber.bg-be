package com.uber.bg.uber.bg.Repositories.Jpa;

import com.uber.bg.uber.bg.Entities.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository <Car, UUID> {
}
