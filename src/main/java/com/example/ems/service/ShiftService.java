package com.example.ems.service;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.shift.*;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.repository.ShiftRepository;
import com.example.ems.repository.ShiftSwapRequestRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShiftService {

    private final ShiftRepository shiftRepo;
    private final ShiftSwapRequestRepository swapRequestRepo;
    private final EmployeeProfileRepository employeeRepo;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    public ShiftService(ShiftRepository shiftRepo, ShiftSwapRequestRepository swapRequestRepo, EmployeeProfileRepository employeeRepo, NotificationService notificationService, UserAccountRepository userAccountRepository) {
        this.shiftRepo = shiftRepo;
        this.swapRequestRepo = swapRequestRepo;
        this.employeeRepo = employeeRepo;
        this.notificationService = notificationService;
        this.userAccountRepository = userAccountRepository;
    }

    // Shift Management
    public List<Shift> getAllShifts() {
        return shiftRepo.findAll();
    }

    public List<Shift> getShiftsByEmployee(Long employeeId) {
        return shiftRepo.findByEmployeeId(employeeId);
    }

    public List<Shift> getShiftsByDate(LocalDate date) {
        return shiftRepo.findByShiftDate(date);
    }

    public List<Shift> getShiftsByEmployeeAndDate(Long employeeId, LocalDate date) {
        return shiftRepo.findByEmployeeIdAndShiftDate(employeeId, date);
    }

    public Shift createDayShift(EmployeeProfile employee, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Enforce fixed day shift: 08:00 to 17:00
        LocalTime fixedStart = LocalTime.of(8, 0);
        LocalTime fixedEnd = LocalTime.of(17, 0);

        if (hasConflict(employee.getId(), date, fixedStart, fixedEnd)) {
            throw new IllegalStateException("Shift conflict detected for employee " + employee.getFirstName() + " " + employee.getLastName());
        }
        
        DayShift shift = new DayShift();
        shift.setEmployee(employee);
        shift.setShiftDate(date);
        shift.setStartTime(fixedStart);
        shift.setEndTime(fixedEnd);
        Shift savedShift = shiftRepo.save(shift);
        
        // Send notification to employee about shift assignment
        notifyEmployeeAboutShiftAssignment(savedShift);
        
        // Send notification to HR/Managers about shift assignment
        notifyHRAboutShiftAssignment(savedShift);
        
        return savedShift;
    }

    public Shift createNightShift(EmployeeProfile employee, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Enforce fixed night shift: 18:00 to 04:00 (overnight)
        LocalTime fixedStart = LocalTime.of(18, 0);
        LocalTime fixedEnd = LocalTime.of(4, 0);

        if (hasConflict(employee.getId(), date, fixedStart, fixedEnd)) {
            throw new IllegalStateException("Shift conflict detected for employee " + employee.getFirstName() + " " + employee.getLastName());
        }
        
        NightShift shift = new NightShift();
        shift.setEmployee(employee);
        shift.setShiftDate(date);
        shift.setStartTime(fixedStart);
        shift.setEndTime(fixedEnd);
        Shift savedShift = shiftRepo.save(shift);
        
        // Send notification to employee about shift assignment
        notifyEmployeeAboutShiftAssignment(savedShift);
        
        // Send notification to HR/Managers about shift assignment
        notifyHRAboutShiftAssignment(savedShift);
        
        return savedShift;
    }

    public Shift updateShift(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Shift shift = shiftRepo.findById(id).orElseThrow();

        // Determine fixed times based on shift type
        LocalTime fixedStart;
        LocalTime fixedEnd;
        if (shift instanceof DayShift) {
            fixedStart = LocalTime.of(8, 0);
            fixedEnd = LocalTime.of(17, 0);
        } else if (shift instanceof NightShift) {
            fixedStart = LocalTime.of(18, 0);
            fixedEnd = LocalTime.of(4, 0);
        } else {
            // Default safeguard: treat as day shift
            fixedStart = LocalTime.of(8, 0);
            fixedEnd = LocalTime.of(17, 0);
        }

        // Check for conflicts excluding the current shift with fixed times
        if (hasConflictExcludingShift(shift.getEmployee().getId(), date, fixedStart, fixedEnd, id)) {
            throw new IllegalStateException("Shift conflict detected");
        }

        shift.setShiftDate(date);
        shift.setStartTime(fixedStart);
        shift.setEndTime(fixedEnd);
        return shiftRepo.save(shift);
    }

    public void deleteShift(Long id) {
        // Check if shift is referenced in any swap requests
        List<ShiftSwapRequest> swapRequests = swapRequestRepo.findAll().stream()
            .filter(swap -> swap.getRequestedShift().getId().equals(id) || 
                           swap.getOfferedShift().getId().equals(id))
            .toList();
        
        if (!swapRequests.isEmpty()) {
            throw new IllegalStateException("Cannot delete shift ID " + id + 
                " because it is referenced in " + swapRequests.size() + " swap request(s). " +
                "Please delete the swap requests first.");
        }
        
        shiftRepo.deleteById(id);
    }

    public void deleteShiftWithSwapRequests(Long id) {
        // Find all swap requests that reference this shift
        List<ShiftSwapRequest> swapRequests = swapRequestRepo.findAll().stream()
            .filter(swap -> swap.getRequestedShift().getId().equals(id) || 
                           swap.getOfferedShift().getId().equals(id))
            .toList();
        
        System.out.println("=== DELETE SHIFT WITH SWAP REQUESTS DEBUG ===");
        System.out.println("Deleting shift ID: " + id);
        System.out.println("Found " + swapRequests.size() + " swap requests to delete");
        
        // Delete all associated swap requests first
        for (ShiftSwapRequest swapRequest : swapRequests) {
            System.out.println("Deleting swap request ID: " + swapRequest.getId());
            swapRequestRepo.delete(swapRequest);
        }
        
        // Now delete the shift
        shiftRepo.deleteById(id);
        System.out.println("Shift deleted successfully!");
        System.out.println("=== END DELETE DEBUG ===");
    }

    public boolean hasConflict(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Shift> existingShifts = shiftRepo.findByEmployeeIdAndShiftDate(employeeId, date);
        return existingShifts.stream()
            .anyMatch(s -> isTimeOverlap(startTime, endTime, s.getStartTime(), s.getEndTime()));
    }

    public boolean hasConflictExcludingShift(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeShiftId) {
        List<Shift> existingShifts = shiftRepo.findByEmployeeIdAndShiftDate(employeeId, date);
        return existingShifts.stream()
            .filter(s -> !s.getId().equals(excludeShiftId))
            .anyMatch(s -> isTimeOverlap(startTime, endTime, s.getStartTime(), s.getEndTime()));
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Handle intervals that may wrap past midnight (e.g., 18:00 -> 04:00)
        int s1 = start1.getHour() * 60 + start1.getMinute();
        int e1 = end1.getHour() * 60 + end1.getMinute();
        int s2 = start2.getHour() * 60 + start2.getMinute();
        int e2 = end2.getHour() * 60 + end2.getMinute();

        if (e1 <= s1) e1 += 24 * 60; // wrap interval 1
        if (e2 <= s2) e2 += 24 * 60; // wrap interval 2

        // Check overlap considering possible wrapping by comparing both alignments
        // Align interval 2 relative to interval 1's day
        boolean overlap = (s1 < e2 && s2 < e1) ||
                          (s1 < (e2 + 24 * 60) && (s2 + 24 * 60) < e1);
        return overlap;
    }

    // Shift Swap Management - SIMPLIFIED VERSION
    public ShiftSwapRequest createSwapRequest(Shift requestedShift, Shift offeredShift, EmployeeProfile requester, String reason) {
        // Create swap request with minimal validation
        ShiftSwapRequest swapRequest = new ShiftSwapRequest();
        swapRequest.setRequestedShift(requestedShift);
        swapRequest.setOfferedShift(offeredShift);
        swapRequest.setRequester(requester);
        swapRequest.setTargetEmployee(requestedShift.getEmployee());
        swapRequest.setReason(reason);
        swapRequest.setStatus(ShiftSwapRequest.Status.PENDING);
        
        return swapRequestRepo.save(swapRequest);
    }
    
    // Simple method to create swap request by IDs
    public ShiftSwapRequest createSwapRequestByIds(Long requestedShiftId, Long offeredShiftId, String reason) {
        Shift requestedShift = shiftRepo.findById(requestedShiftId)
            .orElseThrow(() -> new IllegalArgumentException("Requested shift not found"));
        Shift offeredShift = shiftRepo.findById(offeredShiftId)
            .orElseThrow(() -> new IllegalArgumentException("Offered shift not found"));
        
        // Use the first employee as requester for simplicity
        EmployeeProfile requester = employeeRepo.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No employees found"));
        
        return createSwapRequest(requestedShift, offeredShift, requester, reason);
    }
    
    // Admin method to create swap request for any two employees
    public ShiftSwapRequest createAdminSwapRequest(Long requestedShiftId, Long offeredShiftId, 
                                                  Long employee1Id, Long employee2Id, String reason) {
        Shift requestedShift = shiftRepo.findById(requestedShiftId)
            .orElseThrow(() -> new IllegalArgumentException("Requested shift not found"));
        Shift offeredShift = shiftRepo.findById(offeredShiftId)
            .orElseThrow(() -> new IllegalArgumentException("Offered shift not found"));
        
        EmployeeProfile employee1 = employeeRepo.findById(employee1Id)
            .orElseThrow(() -> new IllegalArgumentException("Employee 1 not found"));
        EmployeeProfile employee2 = employeeRepo.findById(employee2Id)
            .orElseThrow(() -> new IllegalArgumentException("Employee 2 not found"));
        
        // Validate that the shifts belong to the correct employees
        if (!offeredShift.getEmployee().getId().equals(employee1Id)) {
            throw new IllegalArgumentException("Offered shift does not belong to Employee 1");
        }
        if (!requestedShift.getEmployee().getId().equals(employee2Id)) {
            throw new IllegalArgumentException("Requested shift does not belong to Employee 2");
        }
        
        // Find admin user to be the requester
        EmployeeProfile adminRequester = employeeRepo.findAll().stream()
            .filter(emp -> emp.getFirstName().equalsIgnoreCase("admin") || 
                          emp.getEmail().contains("admin"))
            .findFirst()
            .orElse(employeeRepo.findAll().stream().findFirst().orElse(null));
        
        // Create swap request with admin as requester
        ShiftSwapRequest swapRequest = new ShiftSwapRequest();
        swapRequest.setRequestedShift(requestedShift);  // Shift that employee1 wants (currently owned by employee2)
        swapRequest.setOfferedShift(offeredShift);      // Shift that employee1 is giving up
        swapRequest.setRequester(adminRequester);       // Admin is requesting the swap
        swapRequest.setTargetEmployee(employee1);       // Employee1 is the primary target
        swapRequest.setReason("Admin request: " + reason);
        swapRequest.setStatus(ShiftSwapRequest.Status.PENDING);
        
        System.out.println("=== ADMIN SWAP REQUEST DEBUG ===");
        System.out.println("Admin (" + adminRequester.getFirstName() + ") creating swap request");
        System.out.println("Target Employee 1 (" + employee1.getFirstName() + ") giving up shift " + offeredShift.getId());
        System.out.println("Target Employee 2 (" + employee2.getFirstName() + ") giving up shift " + requestedShift.getId());
        System.out.println("Swap request created by admin");
        System.out.println("=== END ADMIN SWAP DEBUG ===");
        
        return swapRequestRepo.save(swapRequest);
    }

    public List<ShiftSwapRequest> getSwapRequestsByRequester(Long requesterId) {
        return swapRequestRepo.findByRequesterId(requesterId);
    }

    public List<ShiftSwapRequest> getSwapRequestsByTarget(Long targetId) {
        return swapRequestRepo.findByTargetEmployeeId(targetId);
    }

    public List<ShiftSwapRequest> getPendingSwapRequests() {
        return swapRequestRepo.findByStatus(ShiftSwapRequest.Status.PENDING);
    }

    public List<ShiftSwapRequest> getApprovedSwapRequests() {
        return swapRequestRepo.findByStatus(ShiftSwapRequest.Status.APPROVED);
    }

    public ShiftSwapRequest approveSwapRequest(Long swapRequestId) {
        ShiftSwapRequest swapRequest = swapRequestRepo.findById(swapRequestId).orElseThrow();
        
        if (!swapRequest.isPending()) {
            throw new IllegalStateException("Swap request is not pending");
        }
        
        // Perform the actual swap
        Shift requestedShift = swapRequest.getRequestedShift();  // The shift the requester wants
        Shift offeredShift = swapRequest.getOfferedShift();      // The shift the requester is offering
        
        EmployeeProfile requester = swapRequest.getRequester();
        EmployeeProfile targetEmployee = swapRequest.getTargetEmployee();
        
        // Get the original employees who own these shifts
        EmployeeProfile originalRequestedShiftEmployee = requestedShift.getEmployee();
        EmployeeProfile originalOfferedShiftEmployee = offeredShift.getEmployee();
        
        System.out.println("=== SWAP APPROVAL DEBUG ===");
        System.out.println("Requested Shift ID: " + requestedShift.getId() + " (currently owned by: " + originalRequestedShiftEmployee.getFirstName() + ")");
        System.out.println("Offered Shift ID: " + offeredShift.getId() + " (currently owned by: " + originalOfferedShiftEmployee.getFirstName() + ")");
        System.out.println("Requester: " + requester.getFirstName() + " " + requester.getLastName());
        System.out.println("Target Employee: " + targetEmployee.getFirstName() + " " + targetEmployee.getLastName());
        
        // Check if this is an admin-created swap request
        boolean isAdminRequest = requester.getFirstName().equalsIgnoreCase("admin") || 
                                requester.getEmail().contains("admin");
        
        if (isAdminRequest) {
            // For admin requests: swap the two target employees directly
            // Employee who owns the offered shift gets the requested shift
            // Employee who owns the requested shift gets the offered shift
            requestedShift.setEmployee(originalOfferedShiftEmployee);  // Owner of offered shift gets requested shift
            offeredShift.setEmployee(originalRequestedShiftEmployee);  // Owner of requested shift gets offered shift
            
            System.out.println("ADMIN SWAP: Swapping target employees directly");
        } else {
            // For regular employee requests: requester gets what they want, target gets what was offered
            requestedShift.setEmployee(requester);           // Requester gets the shift they wanted
            offeredShift.setEmployee(originalRequestedShiftEmployee);  // Original owner of requested shift gets the offered shift
            
            System.out.println("REGULAR SWAP: Requester gets requested shift");
        }
        
        System.out.println("After swap:");
        System.out.println("Requested Shift ID: " + requestedShift.getId() + " (now owned by: " + requestedShift.getEmployee().getFirstName() + ")");
        System.out.println("Offered Shift ID: " + offeredShift.getId() + " (now owned by: " + offeredShift.getEmployee().getFirstName() + ")");
        
        // Save the updated shifts
        shiftRepo.save(requestedShift);
        shiftRepo.save(offeredShift);
        
        System.out.println("Shifts saved successfully!");
        System.out.println("=== END SWAP DEBUG ===");
        
        // Update swap request status
        swapRequest.approve();
        return swapRequestRepo.save(swapRequest);
    }

    public ShiftSwapRequest rejectSwapRequest(Long swapRequestId) {
        ShiftSwapRequest swapRequest = swapRequestRepo.findById(swapRequestId).orElseThrow();
        
        if (!swapRequest.isPending()) {
            throw new IllegalStateException("Swap request is not pending");
        }
        
        swapRequest.reject();
        return swapRequestRepo.save(swapRequest);
    }

    public void deleteSwapRequest(Long swapRequestId) {
        ShiftSwapRequest swapRequest = swapRequestRepo.findById(swapRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Swap request not found"));
        
        System.out.println("=== DELETE SWAP REQUEST DEBUG ===");
        System.out.println("Deleting swap request ID: " + swapRequestId);
        System.out.println("Status: " + swapRequest.getStatus());
        System.out.println("=== END DELETE DEBUG ===");
        
        swapRequestRepo.delete(swapRequest);
    }

    public ShiftSwapRequest cancelSwapRequest(Long swapRequestId) {
        ShiftSwapRequest swapRequest = swapRequestRepo.findById(swapRequestId).orElseThrow();
        
        if (!swapRequest.isPending()) {
            throw new IllegalStateException("Swap request is not pending");
        }
        
        swapRequest.cancel();
        return swapRequestRepo.save(swapRequest);
    }

    // Analytics and Statistics
    public Map<String, Object> getShiftStatistics() {
        List<Shift> allShifts = getAllShifts();
        List<ShiftSwapRequest> allSwapRequests = swapRequestRepo.findAll();
        
        long totalShifts = allShifts.size();
        long dayShifts = allShifts.stream().filter(s -> s instanceof DayShift).count();
        long nightShifts = allShifts.stream().filter(s -> s instanceof NightShift).count();
        
        long totalSwapRequests = allSwapRequests.size();
        long pendingSwapRequests = allSwapRequests.stream().filter(ShiftSwapRequest::isPending).count();
        long approvedSwapRequests = allSwapRequests.stream().filter(ShiftSwapRequest::isApproved).count();
        long rejectedSwapRequests = allSwapRequests.stream().filter(ShiftSwapRequest::isRejected).count();
        
        return Map.of(
            "totalShifts", totalShifts,
            "dayShifts", dayShifts,
            "nightShifts", nightShifts,
            "totalSwapRequests", totalSwapRequests,
            "pendingSwapRequests", pendingSwapRequests,
            "approvedSwapRequests", approvedSwapRequests,
            "rejectedSwapRequests", rejectedSwapRequests
        );
    }

    public List<Map<String, Object>> getTeamRoster(LocalDate date) {
        List<Shift> shifts = getShiftsByDate(date);
        return shifts.stream()
            .map(shift -> Map.of(
                "shift", shift,
                "employee", shift.getEmployee(),
                "shiftType", shift.getClass().getSimpleName(),
                "duration", calculateShiftDuration(shift.getStartTime(), shift.getEndTime())
            ))
            .collect(Collectors.toList());
    }

    private double calculateShiftDuration(LocalTime start, LocalTime end) {
        return java.time.Duration.between(start, end).toHours();
    }
    
    // Helper method to notify employee about shift assignment
    private void notifyEmployeeAboutShiftAssignment(Shift shift) {
        try {
            // Find the employee's user account
            Optional<UserAccount> employeeUser = userAccountRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(shift.getEmployee().getEmail().split("@")[0]))
                .findFirst();
            
            if (employeeUser.isPresent()) {
                String shiftType = shift instanceof DayShift ? "Day" : "Night";
                notificationService.createShiftReminder(
                    employeeUser.get(),
                    "New Shift Assigned",
                    String.format("You have been assigned a %s shift on %s from %s to %s.", 
                        shiftType, 
                        shift.getShiftDate(),
                        shift.getStartTime(),
                        shift.getEndTime())
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send shift notification to employee: " + e.getMessage());
        }
    }
    
    // Helper method to notify HR/Managers about shift assignment
    private void notifyHRAboutShiftAssignment(Shift shift) {
        try {
            // Get all HR and Manager users
            List<UserAccount> hrAndManagers = userAccountRepository.findAll().stream()
                .filter(user -> user.hasAnyRole("HR", "MANAGER", "ADMIN"))
                .toList();
            
            // Send notification to each HR/Manager
            for (UserAccount user : hrAndManagers) {
                String shiftType = shift instanceof DayShift ? "Day" : "Night";
                notificationService.createAttendanceAlert(
                    user,
                    "Shift Assignment Created",
                    String.format("A %s shift has been assigned to %s %s on %s from %s to %s.", 
                        shiftType,
                        shift.getEmployee().getFirstName(),
                        shift.getEmployee().getLastName(),
                        shift.getShiftDate(),
                        shift.getStartTime(),
                        shift.getEndTime())
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send shift notification to HR/Managers: " + e.getMessage());
        }
    }
}
