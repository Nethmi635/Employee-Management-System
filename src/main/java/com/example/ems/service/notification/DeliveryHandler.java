package com.example.ems.service.notification;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;

public interface DeliveryHandler {
    boolean canDeliver(Notification.DeliveryMethod method);
    void deliver(Notification notification, UserAccount recipient);
    String getHandlerName();
}
