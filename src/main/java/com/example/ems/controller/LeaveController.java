package com.example.ems.controller;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.leave.LeaveBalance;
import com.example.ems.domain.leave.LeaveRequest;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.service.LeaveService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/leave")
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeProfileRepository employeeRepo;

    public LeaveController(LeaveService leaveService, EmployeeProfileRepository employeeRepo) {
        this.leaveService = leaveService;
        this.employeeRepo = employeeRepo;
    }

	@GetMapping
    public String list(@RequestParam(required = false) Long employeeId, 
                      @RequestParam(required = false) String status,
                      Model model) {
        List<LeaveRequest> requests;
        if (employeeId != null) {
            requests = leaveService.listByEmployee(employeeId);
        } else if ("PENDING".equals(status)) {
            requests = leaveService.listPending();
        } else {
            requests = leaveService.listAll();
        }
        
        model.addAttribute("requests", requests);
		model.addAttribute("employees", employeeRepo.findAll());
		model.addAttribute("selectedEmployeeId", employeeId);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("leaveTypes", LeaveBalance.LeaveType.values());
		return "leave/list";
	}

	@PostMapping
    public String create(@RequestParam Long employeeId,
						@RequestParam String startDate,
						@RequestParam String endDate,
						@RequestParam String reason,
						@RequestParam LeaveBalance.LeaveType leaveType) {
        try {
            EmployeeProfile e = employeeRepo.findById(employeeId).orElseThrow();
            leaveService.create(e, LocalDate.parse(startDate), LocalDate.parse(endDate), reason, leaveType);
        } catch (Exception ex) {
            // Handle validation errors - in a real app, you'd add flash messages
            System.err.println("Leave creation error: " + ex.getMessage());
        }
		return "redirect:/leave";
	}

	@PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String updateStatus(@PathVariable Long id, 
                             @RequestParam LeaveRequest.Status status,
                             @RequestParam(required = false) String comments) {
        leaveService.updateStatus(id, status, comments);
		return "redirect:/leave";
	}



    @PostMapping("/balances")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public String createBalance(@RequestParam Long employeeId,
                               @RequestParam LeaveBalance.LeaveType leaveType,
                               @RequestParam int totalDays,
                               @RequestParam int year) {
        EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
        leaveService.createLeaveBalance(employee, leaveType, totalDays, year);
        return "redirect:/leave/balances?employeeId=" + employeeId + "&year=" + year;
    }
}


