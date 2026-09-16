package com.uber.bg.uber.bg.Controllers;

import com.stripe.exception.StripeException;
import com.uber.bg.uber.bg.Services.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void createPayment(@PathVariable final UUID rideId) throws StripeException {
        stripeService.createCheckoutSession(rideId);
    }
}
