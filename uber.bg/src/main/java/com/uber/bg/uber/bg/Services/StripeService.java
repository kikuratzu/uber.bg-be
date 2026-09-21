package com.uber.bg.uber.bg.Services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import com.uber.bg.uber.bg.DTOs.PaymentRequestDTO;
import com.uber.bg.uber.bg.DTOs.PaymentResponseDTO;
import com.uber.bg.uber.bg.DTOs.PaymentStatusDTO;
import com.uber.bg.uber.bg.DTOs.PaymentSummaryDTO;
import com.uber.bg.uber.bg.Entities.Payment;
import com.uber.bg.uber.bg.Enumerations.PAYMENT_STATUS;
import com.uber.bg.uber.bg.Repositories.Jpa.RideRepository;
import com.uber.bg.uber.bg.Repositories.Mongo.PaymentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.stripe.model.checkout.Session;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeService {

    private final PaymentRepository paymentRepository;
    private final PriceService priceService;
    private final RideRepository rideRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public StripeService(PaymentRepository paymentRepository, PriceService priceService,RideRepository rideRepository, SimpMessagingTemplate messagingTemplate, @Value("${stripe.secret.key}") String stripeSecretKey) {
        this.paymentRepository = paymentRepository;
        this.priceService = priceService;
        this.rideRepository = rideRepository;
        this.messagingTemplate = messagingTemplate;
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentSummaryDTO createCheckoutSession(final UUID rideId) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5500/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:5500/map.html")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount((long)(priceService.calculateFare(rideId).getFinalPrice() * 100))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("uber-payment")
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setRideId(rideId);
        payment.setUserId(rideRepository.findById(rideId).orElseThrow().getPassenger().getId());
        payment.setPaymentStatus(PAYMENT_STATUS.PENDING);
        payment.setCurrency("eur");
        payment.setStripeSessionId(session.getId());
        payment.setAmountInCents(session.getAmountTotal());
        paymentRepository.save(payment);

        messagingTemplate.convertAndSend(
                "/topic/payment/" + rideId,
                Map.of("sessionId", session.getId(), "sessionUrl", session.getUrl())
        );

        return new PaymentSummaryDTO(session.getId(), session.getAmountTotal(), "eur");
    }

    public PaymentStatusDTO getPaymentStatus(final String sessionId) throws StripeException {
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("no payment with this session id."));

        if (payment.getPaymentStatus() == PAYMENT_STATUS.PENDING) {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus())) {
                payment.setPaymentStatus(PAYMENT_STATUS.SUCCESS);
                paymentRepository.save(payment);
            }
        }

        return new PaymentStatusDTO(
                payment.getPaymentStatus(),
                payment.getAmountInCents(),
                payment.getCurrency());
    }

}
