package com.uber.bg.uber.bg.Entities;

import com.uber.bg.uber.bg.Enumerations.PAYMENT_STATUS;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment extends MongoBaseEntity{
    private UUID rideId;
    private UUID userId;
    private Long amountInCents;
    private String currency;

    private String stripeSessionId;
    @Enumerated(EnumType.STRING)
    private PAYMENT_STATUS paymentStatus;
}
