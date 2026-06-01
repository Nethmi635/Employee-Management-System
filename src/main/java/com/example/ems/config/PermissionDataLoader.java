package com.example.ems.config;

import com.example.ems.domain.user.Permission;
import com.example.ems.repository.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionDataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public PermissionDataLoader(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (permissionRepository.count() == 0) {
            loadDefaultPermissions();
        }
    }

    private void loadDefaultPermissions() {
        List<Permission> defaultPermissions = List.of(
                // User Management Permissions
                new Permission("USER_CREATE", "USER_MANAGEMENT", "Create new user accounts", true),
                new Permission("USER_EDIT", "USER_MANAGEMENT", "Edit existing user accounts", true),
                new Permission("USER_DELETE", "USER_MANAGEMENT", "Delete user accounts", true),
                new Permission("USER_VIEW", "USER_MANAGEMENT", "View user information", true),
                new Permission("ROLE_MANAGE", "USER_MANAGEMENT", "Manage user roles", true),
                new Permission("PERMISSION_MANAGE", "USER_MANAGEMENT", "Manage user permissions", true),

                // Attendance Permissions
                new Permission("ATTENDANCE_VIEW", "ATTENDANCE", "View attendance records", true),
                new Permission("ATTENDANCE_EDIT", "ATTENDANCE", "Edit attendance records", true),
                new Permission("ATTENDANCE_APPROVE", "ATTENDANCE", "Approve attendance corrections", true),
                new Permission("ATTENDANCE_EXPORT", "ATTENDANCE", "Export attendance data", true),

                // Leave Management Permissions
                new Permission("LEAVE_VIEW", "LEAVE", "View leave requests", true),
                new Permission("LEAVE_CREATE", "LEAVE", "Create leave requests", true),
                new Permission("LEAVE_APPROVE", "LEAVE", "Approve/reject leave requests", true),
                new Permission("LEAVE_REJECT", "LEAVE", "Reject leave requests", true),
                new Permission("LEAVE_MANAGE", "LEAVE", "Manage leave policies and balances", true),
                new Permission("LEAVE_EXPORT", "LEAVE", "Export leave data", true),

                // Reports Permissions
                new Permission("REPORTS_VIEW", "REPORTS", "View reports and system data", true),
                new Permission("REPORTS_GENERATE", "REPORTS", "Generate custom reports", true),
                new Permission("REPORTS_EXPORT", "REPORTS", "Export report data", true),
                new Permission("DATA_EXPORT", "REPORTS", "Export system data", true),

                // Performance Permissions
                new Permission("PERFORMANCE_VIEW", "PERFORMANCE", "View performance reviews", true),
                new Permission("PERFORMANCE_CREATE", "PERFORMANCE", "Create performance reviews", true),
                new Permission("PERFORMANCE_EDIT", "PERFORMANCE", "Edit performance reviews", true),
                new Permission("PERFORMANCE_MANAGE", "PERFORMANCE", "Manage performance reviews", true),
                new Permission("PERFORMANCE_APPROVE", "PERFORMANCE", "Approve performance ratings", true),

                // Shift Management Permissions
                new Permission("SHIFT_VIEW", "SHIFT", "View shift schedules", true),
                new Permission("SHIFT_CREATE", "SHIFT", "Create shift schedules", true),
                new Permission("SHIFT_EDIT", "SHIFT", "Edit shift schedules", true),
                new Permission("SHIFT_DELETE", "SHIFT", "Delete shift schedules", true),
                new Permission("SHIFT_ASSIGN", "SHIFT", "Assign shifts to employees", true),

                // Notification Permissions
                new Permission("NOTIFICATIONS_SEND", "NOTIFICATIONS", "Send notifications to users", true),
                new Permission("NOTIFICATIONS_MANAGE", "NOTIFICATIONS", "Manage notification settings", true),
                new Permission("ALERTS_MANAGE", "NOTIFICATIONS", "Manage system alerts", true),

                // System Administration Permissions
                new Permission("SYSTEM_CONFIG", "SYSTEM", "Configure system settings", true),
                new Permission("SYSTEM_BACKUP", "SYSTEM", "Backup system data", true),
                new Permission("SYSTEM_RESTORE", "SYSTEM", "Restore system data", true),
                new Permission("AUDIT_VIEW", "SYSTEM", "View audit logs", true),

                // Employee Management Permissions
                new Permission("EMPLOYEE_VIEW", "EMPLOYEE", "View employee information", true),
                new Permission("EMPLOYEE_CREATE", "EMPLOYEE", "Create employee records", true),
                new Permission("EMPLOYEE_EDIT", "EMPLOYEE", "Edit employee information", true),
                new Permission("EMPLOYEE_DELETE", "EMPLOYEE", "Delete employee records", true),
                new Permission("EMPLOYEE_EXPORT", "EMPLOYEE", "Export employee data", true),
                new Permission("EMPLOYEE_IMPORT", "EMPLOYEE", "Import employee data", true),

                // Profile Management Permissions
                new Permission("PROFILE_VIEW", "PROFILE", "View user profiles", true),
                new Permission("PROFILE_EDIT", "PROFILE", "Edit user profiles", true),
                new Permission("PROFILE_DELETE", "PROFILE", "Delete user profiles", true)
        );

        permissionRepository.saveAll(defaultPermissions);
        System.out.println("Loaded " + defaultPermissions.size() + " default permissions");
    }
}