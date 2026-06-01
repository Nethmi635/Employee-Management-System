package com.example.ems.controller;

import com.example.ems.service.PerformanceService;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/performance/dashboard")
@PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
public class PerformanceDashboardController {

    private final PerformanceService performanceService;
    private final EmployeeProfileRepository employeeRepo;

    public PerformanceDashboardController(PerformanceService performanceService, EmployeeProfileRepository employeeRepo) {
        this.performanceService = performanceService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Long employeeId, Model model) {
        // Get performance statistics
        Map<String, Object> stats = performanceService.getPerformanceStatistics(employeeId);
        
        // Get top performers
        List<Map<String, Object>> topPerformers = performanceService.getTopPerformers(5);
        
        // Get KPI summary
        List<Map<String, Object>> kpiSummary = performanceService.getKpiSummary();
        
        model.addAttribute("stats", stats);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("kpiSummary", kpiSummary);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        
        return "performance/dashboard";
    }
}
