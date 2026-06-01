package com.example.ems.controller;

import com.example.ems.service.UserManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    private final UserManagementService userManagementService;

    public PasswordResetController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String username, Model model) {
        try {
            userManagementService.createPasswordResetToken(username);
            model.addAttribute("success", "Password reset link has been sent to your email (simulated)");
            model.addAttribute("token", "Check console for token in development mode");
        } catch (Exception e) {
            model.addAttribute("error", "User not found or error occurred");
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, 
                               @RequestParam String password, 
                               @RequestParam String confirmPassword,
                               Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        try {
            boolean success = userManagementService.resetPassword(token, password);
            if (success) {
                model.addAttribute("success", "Password has been reset successfully");
                return "auth/login";
            } else {
                model.addAttribute("error", "Invalid or expired token");
                model.addAttribute("token", token);
                return "auth/reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error resetting password: " + e.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }
}
