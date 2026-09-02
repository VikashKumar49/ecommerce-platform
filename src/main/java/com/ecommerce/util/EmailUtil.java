package com.ecommerce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@ecommerce.com");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String email, String token) {
        String subject = "Email Verification - E-Commerce Platform";
        String body = "Click the link below to verify your email:\n\n" +
                "http://localhost:3000/verify?token=" + token + "\n\n" +
                "This link will expire in 24 hours.";
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String token) {
        String subject = "Password Reset - E-Commerce Platform";
        String body = "Click the link below to reset your password:\n\n" +
                "http://localhost:3000/reset-password?token=" + token + "\n\n" +
                "This link will expire in 1 hour.";
        sendEmail(email, subject, body);
    }

    public void sendOrderConfirmationEmail(String email, Long orderId, String customerName) {
        String subject = "Order Confirmation - E-Commerce Platform";
        String body = "Dear " + customerName + ",\n\n" +
                "Thank you for your order!\n\n" +
                "Order ID: " + orderId + "\n" +
                "Status: PENDING\n\n" +
                "You will receive updates on your order status via email.\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        sendEmail(email, subject, body);
    }

    public void sendShippingNotificationEmail(String email, Long orderId, String trackingNumber) {
        String subject = "Your Order is Shipped - E-Commerce Platform";
        String body = "Your order has been shipped!\n\n" +
                "Order ID: " + orderId + "\n" +
                "Tracking Number: " + trackingNumber + "\n\n" +
                "Track your package here: http://tracking.ecommerce.com/" + trackingNumber + "\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        sendEmail(email, subject, body);
    }
}
