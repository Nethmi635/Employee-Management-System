package com.example.ems.service.notification;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class InAppDeliveryHandler implements DeliveryHandler {

    private final NotificationRepository notificationRepository;

    public InAppDeliveryHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public boolean canDeliver(Notification.DeliveryMethod method) {
        return method == Notification.DeliveryMethod.IN_APP;
    }

    @Override
    public void deliver(Notification notification, UserAccount recipient) {
        try {
            // In-app notifications are already saved to database
            // This handler is called for additional processing if needed
            
            // Log the in-app notification delivery
            System.out.println("=== IN-APP NOTIFICATION ===");
            System.out.println("To: " + recipient.getUsername());
            System.out.println("Title: " + notification.getTitle());
            System.out.println("Message: " + notification.getMessage());
            System.out.println("Priority: " + notification.getPriority().getDisplayName());
            System.out.println("Alert Type: " + notification.getAlertType().getDisplayName());
            System.out.println("Created: " + notification.getCreatedAt());
            System.out.println("==========================");
            
            // TODO: In production, additional in-app processing could include:
            // - Real-time WebSocket notifications to connected clients
            // - Browser push notifications for web users
            // - Mobile app push notifications
            // - Notification sound/visual alerts
            // - Badge count updates
            // - Notification grouping and prioritization
            
        } catch (Exception e) {
            System.err.println("Failed to process in-app notification: " + e.getMessage());
        }
    }

    @Override
    public String getHandlerName() {
        return "In-App Delivery";
    }
}
