package com.ecommerce.ecommerce.controller.payment;

import com.ecommerce.ecommerce.service.paymentService.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:5500")
public class PaymentIntegrationController {

    private final PaymentService paymentService;

    public PaymentIntegrationController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public Map<String, String> createRazorpayOrder(@RequestBody Map<String, Object> paymentData) throws RazorpayException {
        int amount = Integer.parseInt(paymentData.get("amount").toString()); // Ensure correct type
        return paymentService.createRazorpayOrder(amount);
    }

    @GetMapping("/verify")
    public boolean verifyPayment(@RequestParam String paymentId) {
        return paymentService.verifyPayment(paymentId);
    }

    @PostMapping("/refund")
    public ResponseEntity<String> refund(@RequestBody Map<String, String> requestData) {
        String paymentId = requestData.get("payment_id");
        String refundResponse = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundResponse);
    }
}
