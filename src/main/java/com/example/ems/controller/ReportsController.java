package com.example.ems.controller;

import com.example.ems.domain.attendance.AttendanceRecord;
import com.example.ems.domain.leave.LeaveRequest;
import com.example.ems.repository.AttendanceRecordRepository;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.repository.LeaveRequestRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
public class ReportsController {

    private final AttendanceRecordRepository attendanceRepo;
    private final LeaveRequestRepository leaveRepo;
    private final EmployeeProfileRepository employeeRepo;

    public ReportsController(AttendanceRecordRepository attendanceRepo, 
                           LeaveRequestRepository leaveRepo,
                           EmployeeProfileRepository employeeRepo) {
        this.attendanceRepo = attendanceRepo;
        this.leaveRepo = leaveRepo;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Get current month data
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        // Get attendance summary
        List<AttendanceRecord> attendanceRecords = attendanceRepo.findByDateBetween(startDate, endDate);
        long totalRecords = attendanceRecords.size();
        long presentCount = attendanceRecords.stream().filter(AttendanceRecord::isPresent).count();
        long absentCount = attendanceRecords.stream().filter(AttendanceRecord::isAbsent).count();
        long partialCount = attendanceRecords.stream().filter(AttendanceRecord::isPartial).count();
        double totalOvertimeHours = attendanceRecords.stream()
            .mapToDouble(AttendanceRecord::getOvertimeHours)
            .sum();
        
        // Get leave summary
        List<LeaveRequest> leaveRequests = leaveRepo.findAll();
        long pendingLeaves = leaveRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.PENDING)
            .count();
        long approvedLeaves = leaveRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
            .count();
        long rejectedLeaves = leaveRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.REJECTED)
            .count();
        
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("partialCount", partialCount);
        model.addAttribute("totalOvertimeHours", totalOvertimeHours);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("approvedLeaves", approvedLeaves);
        model.addAttribute("rejectedLeaves", rejectedLeaves);
        model.addAttribute("employees", employeeRepo.findAll());
        
        return "reports/dashboard";
    }

    @GetMapping("/attendance")
    public String attendanceReport(@RequestParam(required = false) Long employeeId,
                                 @RequestParam(required = false) String month,
                                 Model model) {
        // Parse month parameter or use current month
        YearMonth yearMonth;
        if (month != null && !month.isEmpty()) {
            yearMonth = YearMonth.parse(month);
        } else {
            yearMonth = YearMonth.now();
        }
        
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<AttendanceRecord> records;
        if (employeeId != null) {
            records = attendanceRepo.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        } else {
            records = attendanceRepo.findByDateBetween(startDate, endDate);
        }
        
        // Calculate statistics
        Map<String, Object> stats = calculateAttendanceStats(records);
        
        model.addAttribute("records", records);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("yearMonth", yearMonth);
        model.addAttribute("stats", stats);
        
        return "reports/attendance";
    }

    @GetMapping("/leave")
    public String leaveReport(@RequestParam(required = false) Long employeeId,
                            @RequestParam(required = false) String status,
                            Model model) {
        List<LeaveRequest> requests;
        if (employeeId != null) {
            requests = leaveRepo.findByEmployeeId(employeeId);
        } else if (status != null && !status.isEmpty()) {
            requests = leaveRepo.findByStatus(LeaveRequest.Status.valueOf(status));
        } else {
            requests = leaveRepo.findAll();
        }
        
        // Calculate statistics
        Map<String, Object> stats = calculateLeaveStats(requests);
        
        model.addAttribute("requests", requests);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("stats", stats);
        
        return "reports/leave";
    }

    private Map<String, Object> calculateAttendanceStats(List<AttendanceRecord> records) {
        long totalRecords = records.size();
        long presentCount = records.stream().filter(AttendanceRecord::isPresent).count();
        long absentCount = records.stream().filter(AttendanceRecord::isAbsent).count();
        long partialCount = records.stream().filter(AttendanceRecord::isPartial).count();
        long completeCount = records.stream().filter(AttendanceRecord::isComplete).count();
        
        double totalWorkingHours = records.stream()
            .mapToDouble(AttendanceRecord::getWorkingHours)
            .sum();
        double totalOvertimeHours = records.stream()
            .mapToDouble(AttendanceRecord::getOvertimeHours)
            .sum();
        double averageWorkingHours = completeCount > 0 ? totalWorkingHours / completeCount : 0;
        
        double attendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0;
        
        return Map.of(
            "totalRecords", totalRecords,
            "presentCount", presentCount,
            "absentCount", absentCount,
            "partialCount", partialCount,
            "completeCount", completeCount,
            "totalWorkingHours", totalWorkingHours,
            "totalOvertimeHours", totalOvertimeHours,
            "averageWorkingHours", averageWorkingHours,
            "attendanceRate", attendanceRate
        );
    }

    private Map<String, Object> calculateLeaveStats(List<LeaveRequest> requests) {
        long totalRequests = requests.size();
        long pendingCount = requests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.PENDING)
            .count();
        long approvedCount = requests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
            .count();
        long rejectedCount = requests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.REJECTED)
            .count();
        long cancelledCount = requests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.CANCELLED)
            .count();
        
        int totalDaysRequested = requests.stream()
            .mapToInt(LeaveRequest::getDurationInDays)
            .sum();
        int approvedDays = requests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.Status.APPROVED)
            .mapToInt(LeaveRequest::getDurationInDays)
            .sum();
        
        double approvalRate = totalRequests > 0 ? (double) approvedCount / totalRequests * 100 : 0;
        
        return Map.of(
            "totalRequests", totalRequests,
            "pendingCount", pendingCount,
            "approvedCount", approvedCount,
            "rejectedCount", rejectedCount,
            "cancelledCount", cancelledCount,
            "totalDaysRequested", totalDaysRequested,
            "approvedDays", approvedDays,
            "approvalRate", approvalRate
        );
    }
}
