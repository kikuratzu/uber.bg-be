package com.uber.bg.uber.bg.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.uber.bg.uber.bg.DTOs.PriceDTO;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class PriceService {

    // Pricing constants based on your accessible model
    private static final double BASE_FARE = 1.50;
    private static final double COST_PER_KM = 0.90;
    private static final double BOOKING_FEE = 0.50;
    private static final double MINIMUM_FARE = 4.00;

    private final RestClient restClient = RestClient.create();

    private final RideRepository rideRepository;

    @Autowired
    public PriceService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional(readOnly = true)
    public PriceDTO calculateFare(final UUID rideId) {

        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new IllegalArgumentException("no ride with this id."));

        String osrmUrl = String.format(
                "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                ride.getPickupLocation().getLongitude(),
                ride.getPickupLocation().getLatitude(),
                ride.getDestinationLocation().getLongitude(),
                ride.getDestinationLocation().getLatitude());

        JsonNode response = restClient.get()
                .uri(osrmUrl)
                .retrieve()
                .body(JsonNode.class);

        double distanceInMeters = response.get("routes").get(0).get("distance").asDouble();

        double kilometers = Math.round((distanceInMeters / 1000.0) * 100.0) / 100.0;

        double subtotal = BASE_FARE + (COST_PER_KM * kilometers);

        double totalWithFees = subtotal + BOOKING_FEE;

        double finalPrice = Math.max(totalWithFees, MINIMUM_FARE);

        finalPrice = Math.round(finalPrice * 100.0) / 100.0;

        return new PriceDTO(kilometers, finalPrice);
    }
}