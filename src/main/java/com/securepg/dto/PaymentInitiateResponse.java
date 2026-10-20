package com.securepg.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitiateResponse {

    private String razorpayOrderId;
    private String razorpayKeyId;
    private long amountPaisa;
    private String currency;
}
