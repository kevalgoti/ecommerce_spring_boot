package com.ecommerce.ecommerce.service.paymentService;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    @Value("${rzp.key.id}")
    private String keyId;

    @Value("${rzp.key.secret}")
    private String secret;
    private final RazorpayClient razorpayClient;

    public PaymentService(@Value("${rzp.key.id}") String keyId, @Value("${rzp.key.secret}") String secret) throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, secret);
    }

    // ✅ Create Razorpay Order
    public Map<String, String> createRazorpayOrder(int amount) throws RazorpayException {
        int amountInPaise = amount * 100; // Convert to Paise

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1); // Auto capture payment

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", razorpayOrder.get("id"));
        response.put("currency", "INR");
        response.put("amount", String.valueOf(amount));

        return response;
    }

    public String refundPayment(String paymentId) {
        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, secret);

            // Refund request
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", paymentId);
            refundRequest.put("amount", 1000); // Refund amount in paise (₹10)

            Refund refund = razorpay.payments.refund(refundRequest);
            return refund.toString();
        } catch (RazorpayException e) {
            return "Refund failed: " + e.getMessage();
        }
    }

    // ✅ Verify Payment Status
    public boolean verifyPayment(String paymentId) {
        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            return "captured".equalsIgnoreCase(payment.get("status"));
        } catch (RazorpayException e) {
            return false;
        }
    }
}
