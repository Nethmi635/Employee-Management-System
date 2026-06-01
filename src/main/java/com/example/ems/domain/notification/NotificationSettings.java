package com.example.ems.domain.notification;

import com.example.ems.domain.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private UserAccount user;

    // Approval notifications
    private boolean approvalNotifications = true;
    private boolean approvalEmail = true;
    private boolean approvalSms = false;
    private boolean approvalPush = true;

    // Performance notifications
    private boolean performanceNotifications = true;
    private boolean performanceEmail = true;
    private boolean performanceSms = false;
    private boolean performancePush = true;

    // Attendance notifications
    private boolean attendanceNotifications = true;
    private boolean attendanceEmail = true;
    private boolean attendanceSms = true;
    private boolean attendancePush = true;

    // Shift notifications
    private boolean shiftNotifications = true;
    private boolean shiftEmail = false;
    private boolean shiftSms = true;
    private boolean shiftPush = true;

    // Leave notifications
    private boolean leaveNotifications = true;
    private boolean leaveEmail = true;
    private boolean leaveSms = false;
    private boolean leavePush = true;

    // System notifications
    private boolean systemNotifications = true;
    private boolean systemEmail = true;
    private boolean systemSms = false;
    private boolean systemPush = true;

    // Security notifications
    private boolean securityNotifications = true;
    private boolean securityEmail = true;
    private boolean securitySms = true;
    private boolean securityPush = true;

    // General settings
    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private boolean pushEnabled = true;
    private String emailAddress;
    private String phoneNumber;

    public NotificationSettings(UserAccount user) {
        this.user = user;
    }

    public boolean isDeliveryEnabled(Notification.AlertType alertType, Notification.DeliveryMethod method) {
        return switch (alertType) {
            case APPROVAL -> switch (method) {
                case EMAIL -> approvalEmail && emailEnabled;
                case SMS -> approvalSms && smsEnabled;
                case PUSH -> approvalPush && pushEnabled;
                case IN_APP -> approvalNotifications;
            };
            case PERFORMANCE -> switch (method) {
                case EMAIL -> performanceEmail && emailEnabled;
                case SMS -> performanceSms && smsEnabled;
                case PUSH -> performancePush && pushEnabled;
                case IN_APP -> performanceNotifications;
            };
            case ATTENDANCE -> switch (method) {
                case EMAIL -> attendanceEmail && emailEnabled;
                case SMS -> attendanceSms && smsEnabled;
                case PUSH -> attendancePush && pushEnabled;
                case IN_APP -> attendanceNotifications;
            };
            case SHIFT -> switch (method) {
                case EMAIL -> shiftEmail && emailEnabled;
                case SMS -> shiftSms && smsEnabled;
                case PUSH -> shiftPush && pushEnabled;
                case IN_APP -> shiftNotifications;
            };
            case LEAVE -> switch (method) {
                case EMAIL -> leaveEmail && emailEnabled;
                case SMS -> leaveSms && smsEnabled;
                case PUSH -> leavePush && pushEnabled;
                case IN_APP -> leaveNotifications;
            };
            case SYSTEM -> switch (method) {
                case EMAIL -> systemEmail && emailEnabled;
                case SMS -> systemSms && smsEnabled;
                case PUSH -> systemPush && pushEnabled;
                case IN_APP -> systemNotifications;
            };
            case SECURITY -> switch (method) {
                case EMAIL -> securityEmail && emailEnabled;
                case SMS -> securitySms && smsEnabled;
                case PUSH -> securityPush && pushEnabled;
                case IN_APP -> securityNotifications;
            };
        };
    }
}
