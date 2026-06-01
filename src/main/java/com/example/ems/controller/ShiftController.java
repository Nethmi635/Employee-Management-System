package com.example.ems.controller;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.shift.DayShift;
import com.example.ems.domain.shift.NightShift;
import com.example.ems.domain.shift.Shift;
import com.example.ems.domain.shift.ShiftSwapRequest;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.service.ShiftService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shifts")
public class ShiftController {

	private final ShiftService shiftService;
	private final EmployeeProfileRepository employeeRepo;

	public ShiftController(ShiftService shiftService, EmployeeProfileRepository employeeRepo) {
		this.shiftService = shiftService;
		this.employeeRepo = employeeRepo;
	}

    @GetMapping
    public String list(@RequestParam(required = false) Long employeeId,
                       @RequestParam(required = false) String date,
                       Model model) {
        List<Shift> shifts = employeeId == null ? 
            shiftService.getAllShifts() : shiftService.getShiftsByEmployee(employeeId);
        
        System.out.println("Loading shifts page - Found " + shifts.size() + " shifts");
        for (Shift shift : shifts) {
            System.out.println("Shift ID: " + shift.getId() + ", Employee: " + shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName() + ", Date: " + shift.getShiftDate() + ", Type: " + shift.getClass().getSimpleName());
        }
        
        model.addAttribute("shifts", shifts);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("date", date);
        
        // Add statistics
        Map<String, Object> statistics = shiftService.getShiftStatistics();
        model.addAttribute("statistics", statistics);

        // Add approved swap requests to display
        List<ShiftSwapRequest> approvedSwaps = shiftService.getApprovedSwapRequests();
        model.addAttribute("approvedSwaps", approvedSwaps);
        
        // Add pending swap requests for managers/HR/Admin to approve
        List<ShiftSwapRequest> pendingSwaps = shiftService.getPendingSwapRequests();
        model.addAttribute("pendingSwaps", pendingSwaps);
        return "shift/list";
    }

	@PostMapping("/day")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
	public String addDay(@RequestParam Long employeeId,
					   @RequestParam String date,
					   @RequestParam String start,
					   @RequestParam String end) {
		try {
			System.out.println("Creating day shift - Employee ID: " + employeeId + ", Date: " + date + ", Start: 08:00, End: 17:00");
			EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
			System.out.println("Found employee: " + employee.getFirstName() + " " + employee.getLastName());
			// Use fixed day shift times: 08:00 to 17:00
			Shift createdShift = shiftService.createDayShift(
					employee,
					LocalDate.parse(date),
					LocalTime.of(8, 0),
					LocalTime.of(17, 0)
			);
			System.out.println("Day shift created successfully with ID: " + createdShift.getId());
			return "redirect:/shifts?success=day-shift-created";
		} catch (Exception e) {
			System.out.println("Error creating day shift: " + e.getMessage());
			e.printStackTrace();
			return "redirect:/shifts?error=day-shift-failed&message=" + e.getMessage();
		}
	}

	@PostMapping("/night")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
	public String addNight(@RequestParam Long employeeId,
						 @RequestParam String date,
						 @RequestParam String start,
						 @RequestParam String end) {
		try {
			System.out.println("Creating night shift - Employee ID: " + employeeId + ", Date: " + date + ", Start: 18:00, End: 04:00");
			EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
			System.out.println("Found employee: " + employee.getFirstName() + " " + employee.getLastName());
			// Use fixed night shift times: 18:00 to 04:00 (overnight)
			Shift createdShift = shiftService.createNightShift(
					employee,
					LocalDate.parse(date),
					LocalTime.of(18, 0),
					LocalTime.of(4, 0)
			);
			System.out.println("Night shift created successfully with ID: " + createdShift.getId());
			return "redirect:/shifts?success=night-shift-created";
		} catch (Exception e) {
			System.out.println("Error creating night shift: " + e.getMessage());
			e.printStackTrace();
			return "redirect:/shifts?error=night-shift-failed&message=" + e.getMessage();
		}
	}

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String delete(@PathVariable Long id, @RequestParam(required = false) Long employeeId) {
        try {
            System.out.println("Deleting shift ID: " + id);
            shiftService.deleteShiftWithSwapRequests(id);
            System.out.println("Shift deleted successfully!");
            return "redirect:/shifts?success=shift-deleted";
        } catch (Exception e) {
            System.out.println("Error deleting shift: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/shifts?error=delete-failed&message=" + e.getMessage();
        }
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String deleteGet(@PathVariable Long id) {
        try {
            System.out.println("Deleting shift ID (GET): " + id);
            shiftService.deleteShiftWithSwapRequests(id);
            System.out.println("Shift deleted successfully!");
            return "redirect:/shifts?success=shift-deleted";
        } catch (Exception e) {
            System.out.println("Error deleting shift: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/shifts?error=delete-failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String edit(@PathVariable Long id,
                       @RequestParam String date,
                       @RequestParam String start,
                       @RequestParam String end) {
        try {
            shiftService.updateShift(id, LocalDate.parse(date), LocalTime.parse(start), LocalTime.parse(end));
            return "redirect:/shifts";
        } catch (Exception e) {
            return "redirect:/shifts?error=conflict";
        }
    }

    @PostMapping("/swap-request")
    public String createSwapRequest(@RequestParam Long requestedShiftId,
                                   @RequestParam Long offeredShiftId,
                                   @RequestParam String reason,
                                   @RequestParam(required = false) Long employee1Id,
                                   @RequestParam(required = false) Long employee2Id,
                                   Authentication auth) {
        try {
            // Check if this is an admin creating a swap for other employees
            if (employee1Id != null && employee2Id != null) {
                // Admin creating swap request for two employees
                ShiftSwapRequest swapRequest = shiftService.createAdminSwapRequest(
                    requestedShiftId, offeredShiftId, employee1Id, employee2Id, reason);
                return "redirect:/shifts?success=swap-requested&id=" + swapRequest.getId();
            } else {
                // Regular employee swap request (existing functionality)
                ShiftSwapRequest swapRequest = shiftService.createSwapRequestByIds(requestedShiftId, offeredShiftId, reason);
                return "redirect:/shifts?success=swap-requested&id=" + swapRequest.getId();
            }
        } catch (IllegalArgumentException e) {
            return "redirect:/shifts?error=invalid-request&message=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/shifts?error=swap-failed&message=" + e.getMessage();
        }
    }

    private EmployeeProfile findEmployeeByUsername(String username) {
        // Try to find employee by email matching username
        var byEmail = employeeRepo.findByEmail(username + "@example.com");
        if (byEmail.isPresent()) {
            return byEmail.get();
        }
        
        // Try to find by first name matching username
        var byFirstName = employeeRepo.findByFirstNameIgnoreCase(username);
        if (byFirstName.isPresent()) {
            return byFirstName.get();
        }
        
        // Fallback: return first employee (for demo purposes)
        return employeeRepo.findAll().stream().findFirst().orElse(null);
    }

    @PostMapping("/swap-request/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String approveSwapRequest(@PathVariable Long id) {
        try {
            System.out.println("Approving swap request ID: " + id);
            shiftService.approveSwapRequest(id);
            System.out.println("Swap request approved successfully!");
            return "redirect:/shifts?success=swap-approved";
        } catch (Exception e) {
            System.out.println("Error approving swap request: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/shifts?error=approval-failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/swap-request/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String rejectSwapRequest(@PathVariable Long id) {
        try {
            shiftService.rejectSwapRequest(id);
            return "redirect:/shifts?success=swap-rejected";
        } catch (Exception e) {
            return "redirect:/shifts?error=rejection-failed";
        }
    }

    @PostMapping("/swap-request/{id}/delete")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String deleteSwapRequest(@PathVariable Long id) {
        try {
            System.out.println("Deleting swap request ID: " + id);
            shiftService.deleteSwapRequest(id);
            System.out.println("Swap request deleted successfully!");
            return "redirect:/shifts?success=swap-deleted";
        } catch (Exception e) {
            System.out.println("Error deleting swap request: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/shifts?error=delete-failed&message=" + e.getMessage();
        }
    }

    @GetMapping("/test-swap")
    public String testSwap(Model model) {
        try {
            // Get first two shifts
            List<Shift> shifts = shiftService.getAllShifts();
            if (shifts.size() >= 2) {
                Shift shift1 = shifts.get(0);
                Shift shift2 = shifts.get(1);
                
                // Get first employee as requester
                EmployeeProfile requester = employeeRepo.findAll().get(0);
                
                // Create a test swap request
                ShiftSwapRequest testSwap = shiftService.createSwapRequest(shift1, shift2, requester, "Test swap request");
                
                model.addAttribute("message", "Test swap request created with ID: " + testSwap.getId());
                model.addAttribute("swapRequest", testSwap);
            } else {
                model.addAttribute("message", "Not enough shifts to create test swap request");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error creating test swap: " + e.getMessage());
        }
        
        return "test-swap";
    }

    @GetMapping("/test-swap-simple")
    public String testSwapSimple(Model model) {
        try {
            // Get all shifts and employees
            List<Shift> shifts = shiftService.getAllShifts();
            List<EmployeeProfile> employees = employeeRepo.findAll();
            List<ShiftSwapRequest> pendingSwaps = shiftService.getPendingSwapRequests();
            List<ShiftSwapRequest> approvedSwaps = shiftService.getApprovedSwapRequests();
            
            model.addAttribute("shifts", shifts);
            model.addAttribute("employees", employees);
            model.addAttribute("pendingSwaps", pendingSwaps);
            model.addAttribute("approvedSwaps", approvedSwaps);
            model.addAttribute("message", "System data loaded successfully");
        } catch (Exception e) {
            model.addAttribute("message", "Error loading data: " + e.getMessage());
        }
        
        return "test-swap-simple";
    }

    @PostMapping("/test-create-swap")
    public String testCreateSwap(@RequestParam Long requestedShiftId,
                                @RequestParam Long offeredShiftId,
                                @RequestParam String reason,
                                Model model) {
        try {
            ShiftSwapRequest swapRequest = shiftService.createSwapRequestByIds(requestedShiftId, offeredShiftId, reason);
            model.addAttribute("message", "✅ Swap request created successfully! ID: " + swapRequest.getId());
            model.addAttribute("swapRequest", swapRequest);
        } catch (Exception e) {
            model.addAttribute("message", "❌ Error creating swap request: " + e.getMessage());
        }
        
        // Redirect back to test page with data
        List<Shift> shifts = shiftService.getAllShifts();
        List<ShiftSwapRequest> pendingSwaps = shiftService.getPendingSwapRequests();
        model.addAttribute("shifts", shifts);
        model.addAttribute("pendingSwaps", pendingSwaps);
        
        return "test-swap-simple";
    }
}


