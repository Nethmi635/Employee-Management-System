package com.example.ems.controller;

import com.example.ems.service.ShiftService;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shifts/roster")
public class TeamRosterController {

    private final ShiftService shiftService;
    private final EmployeeProfileRepository employeeRepo;

    public TeamRosterController(ShiftService shiftService, EmployeeProfileRepository employeeRepo) {
        this.shiftService = shiftService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String roster(@RequestParam(required = false) String date, Model model) {
        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        List<Map<String, Object>> teamRoster = shiftService.getTeamRoster(selectedDate);
        Map<String, Object> statistics = shiftService.getShiftStatistics();
        
        model.addAttribute("teamRoster", teamRoster);
        model.addAttribute("statistics", statistics);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("employees", employeeRepo.findAll());
        
        return "shift/roster";
    }
}
