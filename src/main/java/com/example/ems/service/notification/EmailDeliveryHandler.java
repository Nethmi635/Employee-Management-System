package com.example.ems.service.notification;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class EmailDeliveryHandler implements DeliveryHandler {

    @Override
    public boolean canDeliver(Notification.DeliveryMethod method) {
        return method == Notification.DeliveryMethod.EMAIL;
    }

    @Override
    public void deliver(Notification notification, UserAccount recipient) {
        try {
            // Enhanced email delivery with better formatting and logging
            String recipientEmail = getRecipientEmail(recipient);
            String subject = formatEmailSubject(notification);
            String body = formatEmailBody(notification, recipient);
            
            // Log the email details (in production, this would be sent via email service)
            System.out.println("=== EMAIL NOTIFICATION ===");
            System.out.println("To: " + recipientEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Priority: " + notification.getPriority().getDisplayName());
            System.out.println("Alert Type: " + notification.getAlertType().getDisplayName());
            System.out.println("Body:");
            System.out.println(body);
            System.out.println("=========================");
            
            // TODO: In production, integrate with actual email service:
            // - SendGrid, AWS SES, Mailgun, etc.
            // - Use proper email templates
            // - Handle delivery failures and retries
            // - Track delivery status
            
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }
    
    private String getRecipientEmail(UserAccount recipient) {
        // In a real application, this would come from user profile or settings
        return recipient.getUsername() + "@company.com";
    }
    
    private String formatEmailSubject(Notification notification) {
        return String.format("[%s] %s", 
            notification.getPriority().getDisplayName().toUpperCase(),
            notification.getTitle());
    }
    
    private String formatEmailBody(Notification notification, UserAccount recipient) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(recipient.getDisplayName()).append(",\n\n");
        body.append(notification.getMessage()).append("\n\n");
        body.append("Alert Type: ").append(notification.getAlertType().getDisplayName()).append("\n");
        body.append("Priority: ").append(notification.getPriority().getDisplayName()).append("\n");
        body.append("Created: ").append(notification.getCreatedAt()).append("\n");
        if (notification.getExpiresAt() != null) {
            body.append("Expires: ").append(notification.getExpiresAt()).append("\n");
        }
        body.append("\nBest regards,\nSLIIT EMS System");
        return body.toString();
    }

    @Override
    public String getHandlerName() {
        return "Email Delivery";
    }
}
