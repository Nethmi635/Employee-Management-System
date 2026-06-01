package com.example.ems.controller;

import com.example.ems.domain.leave.LeaveBalance;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.service.LeaveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Controller
public class LeaveBalancesController {

    private final LeaveService leaveService;
    private final EmployeeProfileRepository employeeRepo;

    public LeaveBalancesController(LeaveService leaveService, EmployeeProfileRepository employeeRepo) {
        this.leaveService = leaveService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping("/leave/balances")
    public String balances(@RequestParam(required = false) Long employeeId,
                           @RequestParam(required = false, defaultValue = "2024") int year,
                           Model model,
                           Authentication auth) {
        List<LeaveBalance> balances;
        if (employeeId != null) {
            balances = leaveService.getLeaveBalances(employeeId, year);
        } else {
            balances = List.of();
        }

        model.addAttribute("balances", balances);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedYear", year);

        boolean canManage = auth != null && auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_HR") || a.equals("ROLE_ADMIN"));
        model.addAttribute("canManageBalances", canManage);

        return "leave/balances";
    }
}


