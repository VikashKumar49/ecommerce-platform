package com.ecommerce.controller;

import com.ecommerce.dto.ConfirmPaymentRequest;
import com.ecommerce.dto.CreatePaymentIntentRequest;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.dto.RefundRequest;
import com.ecommerce.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Create payment intent for order
     * POST /api/payment/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            PaymentDTO payment = paymentService.createPaymentIntent(request.getOrderId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment intent created successfully");
            response.put("payment", payment);
            response.put("clientSecret", payment.getStripePaymentIntentId()); // Pass to frontend for Stripe.js

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (StripeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to create payment intent: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Confirm payment after user completes Stripe payment
     * POST /api/payment/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        try {
            PaymentDTO payment = paymentService.confirmPayment(request.getOrderId(), request.getPaymentIntentId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment confirmed successfully");
            response.put("payment", payment);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (StripeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Payment confirmation failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payment status for order
     * GET /api/payment/status/{orderId}
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderId) {
        try {
            Map<String, Object> status = paymentService.getPaymentStatus(orderId);
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get payment details by order ID
     * GET /api/payment/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get payment details by payment ID
     * GET /api/payment/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            PaymentDTO payment = paymentService.getPaymentById(id);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Process refund (Admin only)
     * POST /api/payment/refund
     */
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> processRefund(@Valid @RequestBody RefundRequest request) {
        try {
            PaymentDTO payment = paymentService.processRefund(request.getOrderId(), request.getReason());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Refund processed successfully");
            response.put("payment", payment);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (StripeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Refund failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
