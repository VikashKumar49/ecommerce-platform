package com.ecommerce.service;

import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailUtil emailUtil;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /**
     * Initialize Stripe API key
     */
    private void initializeStripe() {
        if (Stripe.apiKey == null) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    /**
     * Create payment intent for Stripe
     */
    public PaymentDTO createPaymentIntent(Long orderId) throws StripeException {
        initializeStripe();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Convert amount to cents for Stripe
        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // Create PaymentIntent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setDescription("Payment for Order #" + orderId)
                .putMetadata("orderId", orderId.toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.STRIPE);
        payment.setStripePaymentIntentId(paymentIntent.getId());

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    /**
     * Confirm payment
     */
    public PaymentDTO confirmPayment(Long orderId, String paymentIntentId) throws StripeException {
        initializeStripe();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        // Retrieve PaymentIntent from Stripe
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        if (paymentIntent.getStatus().equals("succeeded")) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(paymentIntent.getCharges().getData().get(0).getId());

            // Update order status
            order.setStatus(Order.OrderStatus.PROCESSING);
            orderRepository.save(order);

            // Send payment confirmation email
            emailUtil.sendEmail(order.getUser().getEmail(),
                    "Payment Confirmation - Order #" + orderId,
                    "Your payment of $" + payment.getAmount() + " has been successfully processed.\n" +
                            "Order Status: PROCESSING\n" +
                            "Transaction ID: " + payment.getTransactionId());
        } else if (paymentIntent.getStatus().equals("requires_action")) {
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setFailureReason("Additional action required");
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(paymentIntent.getLastPaymentError() != null
                    ? paymentIntent.getLastPaymentError().getMessage()
                    : "Payment failed");
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    /**
     * Get payment by order ID
     */
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return convertToDTO(payment);
    }

    /**
     * Get payment by ID
     */
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return convertToDTO(payment);
    }

    /**
     * Process refund
     */
    public PaymentDTO processRefund(Long orderId, String reason) throws StripeException {
        initializeStripe();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        if (!payment.getStatus().equals(Payment.PaymentStatus.COMPLETED)) {
            throw new IllegalArgumentException("Can only refund completed payments");
        }

        try {
            // Retrieve charge and refund
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            String chargeId = paymentIntent.getCharges().getData().get(0).getId();

            com.stripe.model.Refund.create(new com.stripe.param.RefundCreateParams.Builder()
                    .setCharge(chargeId)
                    .setReason(com.stripe.param.RefundCreateParams.Reason.valueOf(reason.toUpperCase()))
                    .build());

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Send refund email
            emailUtil.sendEmail(order.getUser().getEmail(),
                    "Refund Processed - Order #" + orderId,
                    "Your refund of $" + payment.getAmount() + " has been processed.\n" +
                            "Reason: " + reason);
        } catch (StripeException e) {
            payment.setFailureReason("Refund failed: " + e.getMessage());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    /**
     * Get payment status
     */
    public Map<String, Object> getPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        Map<String, Object> status = new HashMap<>();
        status.put("orderId", orderId);
        status.put("paymentStatus", payment.getStatus());
        status.put("amount", payment.getAmount());
        status.put("paymentMethod", payment.getPaymentMethod());
        status.put("transactionId", payment.getTransactionId());
        status.put("createdAt", payment.getCreatedAt());
        status.put("updatedAt", payment.getUpdatedAt());

        if (payment.getFailureReason() != null) {
            status.put("failureReason", payment.getFailureReason());
        }

        return status;
    }

    /**
     * Convert Payment entity to PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().toString());
        dto.setPaymentMethod(payment.getPaymentMethod().toString());
        dto.setTransactionId(payment.getTransactionId());
        dto.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}
