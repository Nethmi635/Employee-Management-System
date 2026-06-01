package com.example.ems.service.notification;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class PushDeliveryHandler implements DeliveryHandler {

    @Override
    public boolean canDeliver(Notification.DeliveryMethod method) {
        return method == Notification.DeliveryMethod.PUSH;
    }

    @Override
    public void deliver(Notification notification, UserAccount recipient) {
        try {
            // Enhanced push notification delivery with better formatting and logging
            String deviceToken = getDeviceToken(recipient);
            String title = formatPushTitle(notification);
            String body = formatPushBody(notification);
            
            // Log the push notification details (in production, this would be sent via push service)
            System.out.println("=== PUSH NOTIFICATION ===");
            System.out.println("To: " + recipient.getUsername() + " (Token: " + deviceToken + ")");
            System.out.println("Title: " + title);
            System.out.println("Body: " + body);
            System.out.println("Priority: " + notification.getPriority().getDisplayName());
            System.out.println("Alert Type: " + notification.getAlertType().getDisplayName());
            System.out.println("=========================");
            
            // TODO: In production, integrate with actual push notification service:
            // - Firebase Cloud Messaging (FCM) for Android
            // - Apple Push Notification Service (APNS) for iOS
            // - Web Push API for web browsers
            // - Handle device token management
            // - Handle delivery failures and retries
            // - Track delivery status and user engagement
            
        } catch (Exception e) {
            System.err.println("Failed to send push notification: " + e.getMessage());
        }
    }
    
    private String getDeviceToken(UserAccount recipient) {
        // In a real application, this would come from user's device registration
        // For now, return a placeholder
        return "device_token_" + recipient.getId();
    }
    
    private String formatPushTitle(Notification notification) {
        // Add priority indicator for urgent messages
        if (notification.getPriority() == Notification.Priority.URGENT) {
            return "🚨 " + notification.getTitle();
        } else if (notification.getPriority() == Notification.Priority.HIGH) {
            return "⚠️ " + notification.getTitle();
        }
        return notification.getTitle();
    }
    
    private String formatPushBody(Notification notification) {
        // Truncate message if too long for push notification
        String fullMessage = notification.getMessage();
        int maxLength = 100; // Typical push notification body limit
        if (fullMessage.length() > maxLength) {
            return fullMessage.substring(0, maxLength - 3) + "...";
        }
        return fullMessage;
    }

    @Override
    public String getHandlerName() {
        return "Push Delivery";
    }
}
