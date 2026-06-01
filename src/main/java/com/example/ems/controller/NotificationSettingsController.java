package com.example.ems.controller;

import com.example.ems.domain.notification.NotificationSettings;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/notifications/settings")
public class NotificationSettingsController {

    private final NotificationService notificationService;
    private final UserAccountRepository userRepo;

    public NotificationSettingsController(NotificationService notificationService,
                                        UserAccountRepository userRepo) {
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String settings(Authentication auth, Model model) {
        String username = auth.getName();
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        NotificationSettings settings = notificationService.getSettings(user);
        model.addAttribute("settings", settings);
        model.addAttribute("alertTypes", com.example.ems.domain.notification.Notification.AlertType.values());
        model.addAttribute("deliveryMethods", com.example.ems.domain.notification.Notification.DeliveryMethod.values());
        
        return "notification/settings";
    }

    @PostMapping
    public String updateSettings(Authentication auth,
                                @RequestParam(required = false) Boolean approvalNotifications,
                                @RequestParam(required = false) Boolean approvalEmail,
                                @RequestParam(required = false) Boolean approvalSms,
                                @RequestParam(required = false) Boolean approvalPush,
                                @RequestParam(required = false) Boolean performanceNotifications,
                                @RequestParam(required = false) Boolean performanceEmail,
                                @RequestParam(required = false) Boolean performanceSms,
                                @RequestParam(required = false) Boolean performancePush,
                                @RequestParam(required = false) Boolean attendanceNotifications,
                                @RequestParam(required = false) Boolean attendanceEmail,
                                @RequestParam(required = false) Boolean attendanceSms,
                                @RequestParam(required = false) Boolean attendancePush,
                                @RequestParam(required = false) Boolean shiftNotifications,
                                @RequestParam(required = false) Boolean shiftEmail,
                                @RequestParam(required = false) Boolean shiftSms,
                                @RequestParam(required = false) Boolean shiftPush,
                                @RequestParam(required = false) Boolean leaveNotifications,
                                @RequestParam(required = false) Boolean leaveEmail,
                                @RequestParam(required = false) Boolean leaveSms,
                                @RequestParam(required = false) Boolean leavePush,
                                @RequestParam(required = false) Boolean systemNotifications,
                                @RequestParam(required = false) Boolean systemEmail,
                                @RequestParam(required = false) Boolean systemSms,
                                @RequestParam(required = false) Boolean systemPush,
                                @RequestParam(required = false) Boolean securityNotifications,
                                @RequestParam(required = false) Boolean securityEmail,
                                @RequestParam(required = false) Boolean securitySms,
                                @RequestParam(required = false) Boolean securityPush,
                                @RequestParam(required = false) Boolean emailEnabled,
                                @RequestParam(required = false) Boolean smsEnabled,
                                @RequestParam(required = false) Boolean pushEnabled,
                                @RequestParam(required = false) String emailAddress,
                                @RequestParam(required = false) String phoneNumber) {
        
        String username = auth.getName();
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        NotificationSettings settings = notificationService.getSettings(user);
        
        // Update approval settings
        if (approvalNotifications != null) settings.setApprovalNotifications(approvalNotifications);
        if (approvalEmail != null) settings.setApprovalEmail(approvalEmail);
        if (approvalSms != null) settings.setApprovalSms(approvalSms);
        if (approvalPush != null) settings.setApprovalPush(approvalPush);
        
        // Update performance settings
        if (performanceNotifications != null) settings.setPerformanceNotifications(performanceNotifications);
        if (performanceEmail != null) settings.setPerformanceEmail(performanceEmail);
        if (performanceSms != null) settings.setPerformanceSms(performanceSms);
        if (performancePush != null) settings.setPerformancePush(performancePush);
        
        // Update attendance settings
        if (attendanceNotifications != null) settings.setAttendanceNotifications(attendanceNotifications);
        if (attendanceEmail != null) settings.setAttendanceEmail(attendanceEmail);
        if (attendanceSms != null) settings.setAttendanceSms(attendanceSms);
        if (attendancePush != null) settings.setAttendancePush(attendancePush);
        
        // Update shift settings
        if (shiftNotifications != null) settings.setShiftNotifications(shiftNotifications);
        if (shiftEmail != null) settings.setShiftEmail(shiftEmail);
        if (shiftSms != null) settings.setShiftSms(shiftSms);
        if (shiftPush != null) settings.setShiftPush(shiftPush);
        
        // Update leave settings
        if (leaveNotifications != null) settings.setLeaveNotifications(leaveNotifications);
        if (leaveEmail != null) settings.setLeaveEmail(leaveEmail);
        if (leaveSms != null) settings.setLeaveSms(leaveSms);
        if (leavePush != null) settings.setLeavePush(leavePush);
        
        // Update system settings
        if (systemNotifications != null) settings.setSystemNotifications(systemNotifications);
        if (systemEmail != null) settings.setSystemEmail(systemEmail);
        if (systemSms != null) settings.setSystemSms(systemSms);
        if (systemPush != null) settings.setSystemPush(systemPush);
        
        // Update security settings
        if (securityNotifications != null) settings.setSecurityNotifications(securityNotifications);
        if (securityEmail != null) settings.setSecurityEmail(securityEmail);
        if (securitySms != null) settings.setSecuritySms(securitySms);
        if (securityPush != null) settings.setSecurityPush(securityPush);
        
        // Update general settings
        if (emailEnabled != null) settings.setEmailEnabled(emailEnabled);
        if (smsEnabled != null) settings.setSmsEnabled(smsEnabled);
        if (pushEnabled != null) settings.setPushEnabled(pushEnabled);
        if (emailAddress != null && !emailAddress.isEmpty()) settings.setEmailAddress(emailAddress);
        if (phoneNumber != null && !phoneNumber.isEmpty()) settings.setPhoneNumber(phoneNumber);
        
        notificationService.updateSettings(user, settings);
        
        return "redirect:/notifications/settings?success=true";
    }
}
