package com.example.ems.controller;

import com.example.ems.service.ShiftService;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shifts/calendar")
public class ShiftCalendarController {

    private final ShiftService shiftService;
    private final EmployeeProfileRepository employeeRepo;

    public ShiftCalendarController(ShiftService shiftService, EmployeeProfileRepository employeeRepo) {
        this.shiftService = shiftService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String calendar(@RequestParam(required = false) Long employeeId,
                          @RequestParam(required = false) String month,
                          Model model) {
        
        // Parse month parameter or use current month
        YearMonth yearMonth;
        if (month != null && !month.isEmpty()) {
            yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        } else {
            yearMonth = YearMonth.now();
        }
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Get shifts for the month
        List<Map<String, Object>> monthlyShifts = getMonthlyShifts(employeeId, startDate, endDate);
        
        // Generate calendar days (6 weeks = 42 days)
        List<LocalDate> calendarDays = generateCalendarDays(yearMonth);
        
        model.addAttribute("monthlyShifts", monthlyShifts);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentMonth", yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute("calendarDays", calendarDays);
        
        return "shift/calendar";
    }

    private List<Map<String, Object>> getMonthlyShifts(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> shifts = new java.util.ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<com.example.ems.domain.shift.Shift> dayShifts;
            if (employeeId != null) {
                dayShifts = shiftService.getShiftsByEmployeeAndDate(employeeId, date);
            } else {
                dayShifts = shiftService.getShiftsByDate(date);
            }
            
            for (com.example.ems.domain.shift.Shift shift : dayShifts) {
                shifts.add(Map.of(
                    "shift", shift,
                    "date", date,
                    "employee", shift.getEmployee(),
                    "shiftType", shift.getClass().getSimpleName(),
                    "startTime", shift.getStartTime(),
                    "endTime", shift.getEndTime(),
                    "duration", calculateShiftDuration(shift.getStartTime(), shift.getEndTime())
                ));
            }
        }
        
        return shifts;
    }

    private double calculateShiftDuration(java.time.LocalTime start, java.time.LocalTime end) {
        return java.time.Duration.between(start, end).toHours();
    }

    private List<LocalDate> generateCalendarDays(YearMonth yearMonth) {
        List<LocalDate> days = new ArrayList<>();

        // Get the first day of the month
        LocalDate firstDay = yearMonth.atDay(1);

        // Get the first day of the calendar (might be from previous month)
        LocalDate calendarStart = firstDay.with(DayOfWeek.SUNDAY);

        // Generate 42 days (6 weeks)
        for (int i = 0; i < 42; i++) {
            days.add(calendarStart.plusDays(i));
        }

        return days;
    }
}
