package com.uber.bg.uber.bg.Controllers;

import com.stripe.exception.StripeException;
import com.uber.bg.uber.bg.DTOs.PaymentStatusDTO;
import com.uber.bg.uber.bg.DTOs.PaymentSummaryDTO;
import com.uber.bg.uber.bg.Services.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/auth/payment")
public class PaymentController {

    private final StripeService stripeService;

    @Autowired
    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/createPayment/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public PaymentSummaryDTO createPayment(
            @PathVariable final UUID rideId
    ) throws StripeException {
      return stripeService.createCheckoutSession(rideId);
    }

    @GetMapping("/status/{sessionId}")
    @PreAuthorize("hasRole('DRIVER')")
    public PaymentStatusDTO getPaymentStatus(
            @PathVariable final String sessionId
    ) throws StripeException {
        return stripeService.getPaymentStatus(sessionId);
    }
}
