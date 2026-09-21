package com.uber.bg.uber.bg.DTOs;

import com.uber.bg.uber.bg.Enumerations.PAYMENT_STATUS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PaymentStatusDTO {
    private PAYMENT_STATUS paymentStatus;
    private Long amountInCents;
    String currency;
}
