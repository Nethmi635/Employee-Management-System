package com.example.ems.controller;

import com.example.ems.domain.leave.LeaveRequest;
import com.example.ems.service.LeaveService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/approvals")
@PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
public class ApprovalDashboardController {

    private final LeaveService leaveService;

    public ApprovalDashboardController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Get pending leave requests
        List<LeaveRequest> pendingRequests = leaveService.listPending();
        
        // Get recent approvals (last 10)
        List<LeaveRequest> recentApprovals = leaveService.listAll().stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
            .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()))
            .limit(10)
            .toList();
        
        // Get recent rejections (last 10)
        List<LeaveRequest> recentRejections = leaveService.listAll().stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.REJECTED)
            .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()))
            .limit(10)
            .toList();
        
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("recentApprovals", recentApprovals);
        model.addAttribute("recentRejections", recentRejections);
        model.addAttribute("pendingCount", pendingRequests.size());
        
        return "approvals/dashboard";
    }

    @PostMapping("/bulk-approve")
    public String bulkApprove(@RequestParam List<Long> requestIds, 
                             @RequestParam(required = false) String comments) {
        for (Long id : requestIds) {
            leaveService.updateStatus(id, LeaveRequest.Status.APPROVED, comments);
        }
        return "redirect:/approvals";
    }

    @PostMapping("/bulk-reject")
    public String bulkReject(@RequestParam List<Long> requestIds, 
                            @RequestParam(required = false) String comments) {
        for (Long id : requestIds) {
            leaveService.updateStatus(id, LeaveRequest.Status.REJECTED, comments);
        }
        return "redirect:/approvals";
    }
}
