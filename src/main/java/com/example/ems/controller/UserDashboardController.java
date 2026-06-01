package com.example.ems.controller;

import com.example.ems.domain.user.UserAccount;
import com.example.ems.service.UserManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class UserDashboardController {

    private final UserManagementService userManagementService;

    public UserDashboardController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String userType,
                           @RequestParam(required = false) String status,
                           Authentication auth,
                           Model model) {
        
        // Get current user
        String username = auth.getName();
        UserAccount currentUser = userManagementService.getUserByUsername(username).orElseThrow();
        
        // Get statistics
        Map<String, Object> statistics = userManagementService.getUserStatistics();
        
        // Get users based on filters
        List<UserAccount> users;
        if (userType != null && !userType.isEmpty()) {
            users = userManagementService.getUsersByType(userType);
        } else if ("inactive".equals(status)) {
            users = userManagementService.getInactiveUsers();
        } else {
            users = userManagementService.getAllUsers();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("statistics", statistics);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("selectedUserType", userType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("userTypes", List.of("ADMIN", "MANAGER", "EMPLOYEE"));
        
        return "admin/dashboard";
    }
}
