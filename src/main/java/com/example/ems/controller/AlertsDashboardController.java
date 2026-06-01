package com.example.ems.controller;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.NotificationRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/alerts")
@PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
public class AlertsDashboardController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepo;
    private final UserAccountRepository userRepo;

    public AlertsDashboardController(NotificationService notificationService,
                                   NotificationRepository notificationRepo,
                                   UserAccountRepository userRepo) {
        this.notificationService = notificationService;
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Notification.AlertType alertType,
                          @RequestParam(required = false) Notification.Priority priority,
                          @RequestParam(required = false) Boolean unread,
                          Model model) {
        
        // Get all notifications with filtering
        List<Notification> allNotifications = notificationRepo.findAll();
        
        if (alertType != null) {
            allNotifications = allNotifications.stream()
                .filter(n -> n.getAlertType() == alertType)
                .collect(Collectors.toList());
        }
        
        if (priority != null) {
            allNotifications = allNotifications.stream()
                .filter(n -> n.getPriority() == priority)
                .collect(Collectors.toList());
        }
        
        if (unread != null) {
            allNotifications = allNotifications.stream()
                .filter(n -> n.isRead() == !unread)
                .collect(Collectors.toList());
        }
        
        // Calculate statistics
        long totalNotifications = notificationRepo.count();
        long unreadCount = notificationRepo.findAll().stream()
            .filter(n -> !n.isRead())
            .count();
        long expiredCount = notificationRepo.findAll().stream()
            .filter(Notification::isExpired)
            .count();
        
        // Group by alert type
        Map<Notification.AlertType, Long> byAlertType = allNotifications.stream()
            .collect(Collectors.groupingBy(Notification::getAlertType, Collectors.counting()));
        
        // Group by priority
        Map<Notification.Priority, Long> byPriority = allNotifications.stream()
            .collect(Collectors.groupingBy(Notification::getPriority, Collectors.counting()));
        
        // Recent notifications (last 24 hours)
        List<Notification> recentNotifications = allNotifications.stream()
            .filter(n -> n.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1)))
            .collect(Collectors.toList());
        
        model.addAttribute("notifications", allNotifications);
        model.addAttribute("recentNotifications", recentNotifications);
        model.addAttribute("totalNotifications", totalNotifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("byAlertType", byAlertType);
        model.addAttribute("byPriority", byPriority);
        model.addAttribute("alertTypes", Notification.AlertType.values());
        model.addAttribute("priorities", Notification.Priority.values());
        model.addAttribute("selectedAlertType", alertType);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedUnread", unread);
        
        return "alerts/dashboard";
    }
}
