package com.example.ems.controller;

import com.example.ems.domain.attendance.AttendanceRecord;
import com.example.ems.repository.AttendanceRecordRepository;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance/calendar")
public class AttendanceCalendarController {

    private final AttendanceRecordRepository attendanceRepo;
    private final EmployeeProfileRepository employeeRepo;

    public AttendanceCalendarController(AttendanceRecordRepository attendanceRepo, EmployeeProfileRepository employeeRepo) {
        this.attendanceRepo = attendanceRepo;
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
        
        // Get attendance records for the month
        List<AttendanceRecord> records;
        if (employeeId != null) {
            records = attendanceRepo.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        } else {
            records = attendanceRepo.findByDateBetween(startDate, endDate);
        }
        
        // Group records by date
        Map<LocalDate, List<AttendanceRecord>> recordsByDate = records.stream()
            .collect(Collectors.groupingBy(AttendanceRecord::getDate));
        
        // Generate calendar days (6 weeks = 42 days)
        List<LocalDate> calendarDays = generateCalendarDays(yearMonth);
        
        model.addAttribute("recordsByDate", recordsByDate);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentMonth", yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute("calendarDays", calendarDays);
        
        return "attendance/calendar";
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
