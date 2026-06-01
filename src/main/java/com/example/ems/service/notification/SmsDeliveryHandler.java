package com.example.ems.service.notification;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class SmsDeliveryHandler implements DeliveryHandler {

    @Override
    public boolean canDeliver(Notification.DeliveryMethod method) {
        return method == Notification.DeliveryMethod.SMS;
    }

    @Override
    public void deliver(Notification notification, UserAccount recipient) {
        try {
            // Enhanced SMS delivery with better formatting and logging
            String recipientPhone = getRecipientPhone(recipient);
            String message = formatSmsMessage(notification);
            
            // Log the SMS details (in production, this would be sent via SMS service)
            System.out.println("=== SMS NOTIFICATION ===");
            System.out.println("To: " + recipientPhone);
            System.out.println("Priority: " + notification.getPriority().getDisplayName());
            System.out.println("Alert Type: " + notification.getAlertType().getDisplayName());
            System.out.println("Message: " + message);
            System.out.println("=======================");
            
            // TODO: In production, integrate with actual SMS service:
            // - Twilio, AWS SNS, Vonage, etc.
            // - Handle character limits (160 chars for SMS)
            // - Handle delivery failures and retries
            // - Track delivery status
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS notification: " + e.getMessage());
        }
    }
    
    private String getRecipientPhone(UserAccount recipient) {
        // In a real application, this would come from user profile or settings
        // For now, return a placeholder
        return "+1234567890";
    }
    
    private String formatSmsMessage(Notification notification) {
        // SMS messages should be concise due to character limits
        StringBuilder message = new StringBuilder();
        message.append(notification.getTitle()).append(": ");
        
        // Truncate message if too long for SMS
        String fullMessage = notification.getMessage();
        int maxLength = 140 - message.length(); // Leave room for sender info
        if (fullMessage.length() > maxLength) {
            message.append(fullMessage.substring(0, maxLength - 3)).append("...");
        } else {
            message.append(fullMessage);
        }
        
        // Add priority indicator for urgent messages
        if (notification.getPriority() == Notification.Priority.URGENT) {
            message.insert(0, "URGENT: ");
        }
        
        return message.toString();
    }

    @Override
    public String getHandlerName() {
        return "SMS Delivery";
    }
}
