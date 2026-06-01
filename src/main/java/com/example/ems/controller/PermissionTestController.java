package com.example.ems.controller;

import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.PermissionAwareUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/permission-test")
public class PermissionTestController {

    private final UserAccountRepository userAccountRepository;
    private final PermissionAwareUserService permissionAwareUserService;

    public PermissionTestController(UserAccountRepository userAccountRepository, 
                                  PermissionAwareUserService permissionAwareUserService) {
        this.userAccountRepository = userAccountRepository;
        this.permissionAwareUserService = permissionAwareUserService;
    }

    @GetMapping
    public String testPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
            
            if (user != null) {
                model.addAttribute("username", username);
                model.addAttribute("userType", user.getUserType());
                model.addAttribute("roles", user.getRoles());
                model.addAttribute("effectivePermissions", permissionAwareUserService.getAllEffectivePermissions(user));
                
                // Test specific permissions
                model.addAttribute("hasEmployeeView", permissionAwareUserService.hasPermission(user, "EMPLOYEE_VIEW"));
                model.addAttribute("hasEmployeeCreate", permissionAwareUserService.hasPermission(user, "EMPLOYEE_CREATE"));
                model.addAttribute("hasEmployeeEdit", permissionAwareUserService.hasPermission(user, "EMPLOYEE_EDIT"));
                model.addAttribute("hasAttendanceView", permissionAwareUserService.hasPermission(user, "ATTENDANCE_VIEW"));
                model.addAttribute("hasReportsView", permissionAwareUserService.hasPermission(user, "REPORTS_VIEW"));
                model.addAttribute("hasShiftView", permissionAwareUserService.hasPermission(user, "SHIFT_VIEW"));
                model.addAttribute("hasLeaveView", permissionAwareUserService.hasPermission(user, "LEAVE_VIEW"));
            }
        }
        return "permission-test";
    }

    @GetMapping("/employee-view")
    @PreAuthorize("hasPermission(null, 'EMPLOYEE_VIEW') or hasRole('HR') or hasRole('ADMIN')")
    public String testEmployeeView() {
        return "redirect:/permission-test?message=EMPLOYEE_VIEW permission granted!";
    }

    @GetMapping("/employee-create")
    @PreAuthorize("hasPermission(null, 'EMPLOYEE_CREATE') or hasRole('HR') or hasRole('ADMIN')")
    public String testEmployeeCreate() {
        return "redirect:/permission-test?message=EMPLOYEE_CREATE permission granted!";
    }

    @GetMapping("/reports-view")
    @PreAuthorize("hasPermission(null, 'REPORTS_VIEW') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public String testReportsView() {
        return "redirect:/permission-test?message=REPORTS_VIEW permission granted!";
    }

    @GetMapping("/attendance-view")
    @PreAuthorize("hasPermission(null, 'ATTENDANCE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public String testAttendanceView() {
        return "redirect:/permission-test?message=ATTENDANCE_VIEW permission granted!";
    }
    
    @GetMapping("/shift-view")
    @PreAuthorize("hasPermission(null, 'SHIFT_VIEW') or hasRole('HR') or hasRole('ADMIN')")
    public String testShiftView() {
        return "redirect:/permission-test?message=SHIFT_VIEW permission granted!";
    }
    
    @GetMapping("/leave-view")
    @PreAuthorize("hasPermission(null, 'LEAVE_VIEW') or hasRole('HR') or hasRole('ADMIN')")
    public String testLeaveView() {
        return "redirect:/permission-test?message=LEAVE_VIEW permission granted!";
    }
    
}
