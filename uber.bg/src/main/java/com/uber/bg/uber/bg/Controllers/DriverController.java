package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ActivityDTO;
import com.uber.bg.uber.bg.DTOs.CarDTO;
import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.DTOs.ProfileDTO;
import com.uber.bg.uber.bg.Entities.Ride;
import com.uber.bg.uber.bg.Entities.UserPrincipal;
import com.uber.bg.uber.bg.Services.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/auth/driver")
public class DriverController {

    private final DriverService service;
    private final KafkaTemplate<String, LocationPingDTO> kafkaTemplate;
    private static final String TOPIC = "driver-locations";

    @Autowired
    public DriverController(DriverService driverService, final KafkaTemplate<String, LocationPingDTO> kafkaTemplate){
        this.service = driverService;
        this.kafkaTemplate = kafkaTemplate;
    }

@GetMapping("/getRide/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public Map<String, Object> getRide(
            @PathVariable final UUID rideId
) {
        return service.getRideDetails(rideId);
    }

    @GetMapping("/getAllRides")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    public List<Map<String, String>> getAllRidesAvailableRides() {
    return service.getAllAvailableRides();
}

@PostMapping("/acceptRide/{rideId}/{carId}")
    @PreAuthorize("hasRole('DRIVER')")
    public HttpStatus acceptRide(
        @PathVariable final UUID rideId,
        @PathVariable final UUID carId,
        @AuthenticationPrincipal UserPrincipal userPrincipal
        ) {
        service.acceptRide(rideId, userPrincipal.getId() ,carId);
        return HttpStatus.ACCEPTED;
}

@PostMapping("/streamLocation/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public void streamLocation(
        @PathVariable final UUID rideId,
        @RequestBody final LocationPingDTO locationPingDTO,
        @AuthenticationPrincipal UserPrincipal userPrincipal
        ) {
    ProducerRecord<String, LocationPingDTO> record = new ProducerRecord<>(TOPIC, userPrincipal.getId().toString(), locationPingDTO);
    record.headers().add("rideId", rideId.toString().getBytes(StandardCharsets.UTF_8));

    this.kafkaTemplate.send(record);
}

@DeleteMapping("/endRide/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public void endRide(
            @PathVariable final UUID rideId
) {
service.endRide(rideId);
}

@PostMapping("/goOnline")
    @PreAuthorize("hasRole('DRIVER')")
    public void goOnline(
            @RequestBody final LocationPingDTO dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
) {
        service.goOnline(userPrincipal.getId(), dto);
}

@DeleteMapping("/goOffline")
    @PreAuthorize("hasRole('DRIVER')")
    public void goOffline(
            @AuthenticationPrincipal UserPrincipal userPrincipal
) {
        service.goOffline(userPrincipal.getId());
}

  @GetMapping("/getActivity")
    @PreAuthorize("hasRole('DRIVER')")
    public Page<ActivityDTO> getActivity(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) final Pageable pageable
  ) {
        return service.getActivity(userPrincipal.getId(), pageable);
    }

    @GetMapping("/getCars")
    @PreAuthorize("hasRole('DRIVER')")
    public Set<CarDTO> getCarsByDriverId(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return service.getCarsByDriverId(userPrincipal.getId());
    }

    @PutMapping("/addCar")
    @PreAuthorize("hasRole('DRIVER')")
    public void addCarByDriverId(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody final CarDTO carDTO
    ) {
        service.addCarByDriverId(userPrincipal.getId(), carDTO);
    }

}
