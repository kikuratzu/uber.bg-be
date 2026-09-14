package com.uber.bg.uber.bg.Repositories.Mongo;

import com.uber.bg.uber.bg.Entities.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID> {
    Payment findByRideId(UUID rideId);
}
