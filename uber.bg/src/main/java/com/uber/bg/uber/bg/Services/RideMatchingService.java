package com.uber.bg.uber.bg.Services;


import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Distance;

import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.awt.*;


@Slf4j
@Service
public class RideMatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RideRepository rideRepository;

    private static final String GEO_INDEX_KEY = "drivers:active";

    public RideMatchingService(RedisTemplate<String, Object> redisTemplate,
                               SimpMessagingTemplate simpMessagingTemplate,
                               RideRepository rideRepository) {
        this.redisTemplate = redisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.rideRepository = rideRepository;
    }

    public List<Map<String, Object>> getOnlineDrivers() {
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().search("drivers:active",
                        GeoReference.fromCoordinate(new Point(0,0)),
                        new Distance(20000, Metrics.KILOMETERS),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs());

        if (results == null) return Collections.emptyList();

        List<Map<String, Object>> drivers = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            drivers.add(Map.of(
                    "driverId", result.getContent().getName().toString(),
                    "latitude", result.getContent().getPoint().getY(),
                    "longitude", result.getContent().getPoint().getX()
            ));
        }
        return drivers;
    }

    public void notifyThreeClosestDrivers(final UUID rideId, final int people, final double pickupLng, final double pickupLat, final double destinationLng, final double destinationLat) {
        log.info("Initiating proximity search to notify the 3 closest drivers for rideId: {}", rideId);

        Point passengerLocation = new Point(pickupLng, pickupLat);

        GeoReference<Object> searchCenter = GeoReference.fromCoordinate(passengerLocation);

        Distance searchRadius = new Distance(5.0, Metrics.KILOMETERS);

        RedisGeoCommands.GeoSearchCommandArgs searchArgs = RedisGeoCommands.GeoSearchCommandArgs
                .newGeoSearchArgs()
                .sortAscending()
                .limit(3);

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().search("drivers:active", searchCenter, searchRadius, searchArgs);

        if (results == null || results.getContent().isEmpty()) {
            log.warn("No available online drivers found within a 5km matching zone for rideId: {}", rideId);
            simpMessagingTemplate.convertAndSend("/topic/passenger/" + rideId.toString(), "NO_DRIVERS_AVAILABLE");
            return;
        }

        List<String> targetedDriverIds = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            String driverIdStr = result.getContent().getName().toString();
            targetedDriverIds.add(driverIdStr);
        }

        log.info("Found {} nearby candidate drivers within radius. Emitting WebSocket notifications...", targetedDriverIds.size());

        Map<String, Object> notificationPayload = Map.of(
                "rideId", rideId.toString(),
                "pickupLongitude", pickupLng,
                "pickupLatitude", pickupLat,
                "destinationLongitude", destinationLng,
                "destinationLatitude", destinationLat,
                "people", people,
                "message", "New ride request available near your current location!"
        );

        for (String targetDriverId : targetedDriverIds) {
            String destinationChannel = "/topic/driver/alerts/" + targetDriverId;
            try {
                simpMessagingTemplate.convertAndSend(destinationChannel, notificationPayload);
                log.debug("Dispatched notification popup payload package to channel: {}", destinationChannel);
            } catch (Exception e) {
                log.error("Failed executing WebSocket frame delivery targeting channel routing lane {}", destinationChannel, e);
            }

    }
    }
}
