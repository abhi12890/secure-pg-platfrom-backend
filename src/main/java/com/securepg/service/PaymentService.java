package com.securepg.service;

import com.securepg.dto.PaymentInitiateResponse;
import com.securepg.entity.MonthlyInvoice;
import com.securepg.entity.PaymentStatus;
import com.securepg.entity.TenantProfile;
import com.securepg.exception.BusinessException;
import com.securepg.repository.MonthlyInvoiceRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ObjectProvider<RazorpayClient> razorpayClientProvider;
    private final MonthlyInvoiceRepository monthlyInvoiceRepository;
    private final InvoiceService invoiceService;
    private final TenantService tenantService;
    private final NotificationService notificationService;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    @Transactional
    public PaymentInitiateResponse initiatePayment(UUID invoiceId, TenantProfile tenant) {
        MonthlyInvoice invoice = invoiceService.requireOwnedInvoice(invoiceId, tenant);

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException("Invoice is already paid");
        }

        BigDecimal pending = invoice.getTotalAmountDue().subtract(invoice.getAmountPaid());
        long amountPaisa = pending.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        if (amountPaisa <= 0) {
            throw new BusinessException("No outstanding balance on invoice");
        }

        try {
            RazorpayClient razorpayClient = razorpayClientProvider.getIfAvailable();
            if (razorpayClient == null) {
                throw new BusinessException("Payment gateway is not configured");
            }

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaisa);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "inv-" + invoice.getId());
            orderRequest.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("invoiceId", invoice.getId().toString());
            notes.put("tenantProfileId", tenant.getId().toString());
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            invoice.setRazorpayOrderId(orderId);
            monthlyInvoiceRepository.save(invoice);

            return PaymentInitiateResponse.builder()
                    .razorpayOrderId(orderId)
                    .razorpayKeyId(razorpayKeyId)
                    .amountPaisa(amountPaisa)
                    .currency("INR")
                    .build();
        } catch (RazorpayException e) {
            throw new BusinessException("Unable to initiate payment order");
        }
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new BusinessException("Payment webhook is not configured");
        }
        try {
            Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (RazorpayException e) {
            log.warn("Webhook signature verification failed");
            throw new BusinessException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event");

        if (!"order.paid".equals(eventType)) {
            return;
        }

        JSONObject payloadObj = event.getJSONObject("payload");
        JSONObject orderEntity = payloadObj.getJSONObject("order").getJSONObject("entity");
        JSONObject notes = orderEntity.optJSONObject("notes");

        if (notes == null || !notes.has("invoiceId")) {
            log.warn("order.paid webhook missing invoiceId metadata");
            return;
        }

        UUID invoiceId = UUID.fromString(notes.getString("invoiceId"));
        String razorpayOrderId = orderEntity.getString("id");
        MonthlyInvoice invoice = monthlyInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Invoice not found for webhook"));

        if (!razorpayOrderId.equals(invoice.getRazorpayOrderId())) {
            log.warn("Webhook order does not match the invoice order record");
            throw new BusinessException("Webhook order does not match invoice");
        }

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        invoiceService.markInvoicePaid(invoice);

        TenantProfile tenant = invoice.getTenant();
        String residentName = tenantService.decryptName(tenant);
        String monthName = Month.of(invoice.getBillingMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        notificationService.notifyOwner(
                String.format("Resident %s paid rent for %s %d", residentName, monthName, invoice.getBillingYear()),
                "PAYMENT_SUCCESS");
    }
}
