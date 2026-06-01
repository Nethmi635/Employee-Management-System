package com.example.ems.service;

import com.example.ems.domain.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class PermissionAwareUserService {

    private final PermissionService permissionService;

    public PermissionAwareUserService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Enhanced permission checking that combines role-based and dynamic permissions
     */
    public boolean hasPermission(UserAccount user, String permissionName) {
        // First check dynamic permissions
        if (permissionService.hasPermission(user, permissionName)) {
            return true;
        }
        
        // Fall back to role-based permissions
        return user.getPermissions().contains(permissionName);
    }

    public boolean hasAnyPermission(UserAccount user, String... permissionNames) {
        for (String permissionName : permissionNames) {
            if (hasPermission(user, permissionName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermissions(UserAccount user, String... permissionNames) {
        for (String permissionName : permissionNames) {
            if (!hasPermission(user, permissionName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if user can access a specific resource based on permissions
     */
    public boolean canAccessResource(UserAccount user, String resource) {
        // Check if user has explicit access to the resource
        if (user.canAccess(resource)) {
            return true;
        }

        // Check specific permissions for different resource types
        return switch (resource) {
            case "/admin/**" -> hasPermission(user, "USER_MANAGEMENT") || hasPermission(user, "SYSTEM_CONFIG");
            case "/attendance/**" -> hasPermission(user, "ATTENDANCE_VIEW") || hasPermission(user, "ATTENDANCE_EDIT");
            case "/leave/**" -> hasPermission(user, "LEAVE_VIEW") || hasPermission(user, "LEAVE_APPROVE");
            case "/reports/**" -> hasPermission(user, "REPORTS_VIEW") || hasPermission(user, "REPORTS_GENERATE");
            case "/performance/**" -> hasPermission(user, "PERFORMANCE_VIEW") || hasPermission(user, "PERFORMANCE_MANAGE");
            case "/shifts/**" -> hasPermission(user, "SHIFTS_VIEW") || hasPermission(user, "SHIFTS_MANAGE");
            case "/notifications/**" -> hasPermission(user, "NOTIFICATIONS_SEND") || hasPermission(user, "NOTIFICATIONS_MANAGE");
            case "/employee/**" -> hasPermission(user, "EMPLOYEE_VIEW") || hasPermission(user, "EMPLOYEE_EDIT");
            case "/profile/**" -> hasPermission(user, "PROFILE_VIEW") || hasPermission(user, "PROFILE_EDIT");
            default -> false;
        };
    }

    /**
     * Get all effective permissions for a user (role-based + dynamic)
     */
    public java.util.Set<String> getAllEffectivePermissions(UserAccount user) {
        java.util.Set<String> allPermissions = new java.util.HashSet<>(user.getPermissions());
        allPermissions.addAll(permissionService.getUserPermissions(user));
        return allPermissions;
    }
}
