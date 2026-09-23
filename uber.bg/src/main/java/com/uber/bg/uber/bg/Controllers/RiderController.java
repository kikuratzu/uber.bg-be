package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ActivityDTO;
import com.uber.bg.uber.bg.DTOs.LocationPingDTO;
import com.uber.bg.uber.bg.DTOs.RequestRideDTO;
import com.uber.bg.uber.bg.DTOs.StreamLocationForPassengerDTO;
import com.uber.bg.uber.bg.Entities.LocationPing;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Entities.UserPrincipal;
import com.uber.bg.uber.bg.Enumerations.RIDE_STATUS;
import com.uber.bg.uber.bg.Services.RiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/auth/passenger/")
public class RiderController {


    private final RiderService service;

    @Autowired
    public RiderController(RiderService riderService) {
        this.service = riderService;
    }

    @PostMapping("requestRide")
    @PreAuthorize("hasRole('PASSENGER')")
    public UUID requestRider(
            @RequestBody RequestRideDTO dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
      return service.requestRide(userPrincipal.getId(), dto.getPickup(), dto.getDestination(), dto.getPeople());

    }

    @DeleteMapping("cancelRide")
    @PreAuthorize("hasRole('PASSENGER')")
    public HttpStatus cancelRide(
           @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        service.CancelRide(userPrincipal.getId());
        return HttpStatus.ACCEPTED;
    }

    @PatchMapping("rateRide/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public void rateRide(
            @PathVariable final UUID rideId,
            @RequestParam double rating
    ) {
        service.rateRide(rideId, rating);
    }


    @GetMapping("/getActivity")
    @PreAuthorize("hasAnyRole('DRIVER','PASSENGER')")
    public Page<ActivityDTO> getActivity(
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) final Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return service.getActivity(userPrincipal.getId(), pageable);
    }

    @GetMapping("/getRideStatus/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public String getRideStatus(
            @PathVariable final UUID rideId
    ) {
        return service.getRideStatus(rideId);
    }

    @GetMapping("getDriverDetails/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public StreamLocationForPassengerDTO getDriverDetails(
            @PathVariable final UUID rideId
    ) {
        return service.getDriverDetails(rideId);
    }

    @PutMapping("rateDriver/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public void rateDriver(
            @PathVariable final UUID rideId,
            @RequestParam final double rating
    ) {
        service.rateDriver(rideId, rating);
    }

    @GetMapping("getRideAndCarDetails/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public StreamLocationForPassengerDTO getRideAndCarDetails(
            @PathVariable final UUID rideId
    ) {
        return service.getDriverAndCarInfoAcceptedRide(rideId);
    }

}

