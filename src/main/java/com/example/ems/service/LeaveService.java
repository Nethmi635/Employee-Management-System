package com.example.ems.service;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.leave.LeaveBalance;
import com.example.ems.domain.leave.LeaveRequest;
import com.example.ems.repository.LeaveBalanceRepository;
import com.example.ems.repository.LeaveRequestRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LeaveService {

	private final LeaveRequestRepository leaveRepo;
	private final LeaveBalanceRepository leaveBalanceRepo;
	private final NotificationService notificationService;
	private final UserAccountRepository userAccountRepository;

	public LeaveService(LeaveRequestRepository leaveRepo, LeaveBalanceRepository leaveBalanceRepo, NotificationService notificationService, UserAccountRepository userAccountRepository) {
		this.leaveRepo = leaveRepo;
		this.leaveBalanceRepo = leaveBalanceRepo;
		this.notificationService = notificationService;
		this.userAccountRepository = userAccountRepository;
	}

	public List<LeaveRequest> listAll() {
		return leaveRepo.findAll();
	}

	public List<LeaveRequest> listByEmployee(Long employeeId) {
		return leaveRepo.findByEmployeeId(employeeId);
	}

	public List<LeaveRequest> listPending() {
		return leaveRepo.findByStatus(LeaveRequest.Status.PENDING);
	}

	public LeaveRequest create(EmployeeProfile employee, LocalDate start, LocalDate end, String reason, LeaveBalance.LeaveType leaveType) {
		if (end.isBefore(start)) throw new IllegalArgumentException("End date before start date");
		
		// Check for overlapping requests
		boolean overlap = leaveRepo.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
			employee.getId(), List.of(LeaveRequest.Status.PENDING, LeaveRequest.Status.APPROVED), end, start);
		if (overlap) throw new IllegalStateException("Overlapping leave request exists");
		
		// Check leave balance
		int requestedDays = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
		int currentYear = start.getYear();
		Optional<LeaveBalance> balance = leaveBalanceRepo.findByEmployeeIdAndLeaveTypeAndYear(
			employee.getId(), leaveType, currentYear);
		
		if (balance.isPresent() && !balance.get().hasAvailableDays(requestedDays)) {
			throw new IllegalStateException("Insufficient leave balance. Available: " + 
				balance.get().getRemainingDays() + " days, Requested: " + requestedDays + " days");
		}
		
		LeaveRequest r = new LeaveRequest();
		r.setEmployee(employee);
		r.setStartDate(start);
		r.setEndDate(end);
		r.setReason(reason);
		r.setLeaveType(leaveType);
		return leaveRepo.save(r);
	}

	@PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
	public LeaveRequest updateStatus(Long id, LeaveRequest.Status status, String comments) {
		LeaveRequest r = leaveRepo.findById(id).orElseThrow();
		LeaveRequest.Status oldStatus = r.getStatus();
		r.setStatus(status);
		r.setComments(comments);
		
		// Update leave balance if approved
		if (oldStatus != LeaveRequest.Status.APPROVED && status == LeaveRequest.Status.APPROVED) {
			updateLeaveBalance(r);
		}
		// Restore leave balance if rejected or cancelled after approval
		else if (oldStatus == LeaveRequest.Status.APPROVED && (status == LeaveRequest.Status.REJECTED || status == LeaveRequest.Status.CANCELLED)) {
			restoreLeaveBalance(r);
		}
		
		LeaveRequest savedRequest = leaveRepo.save(r);
		
		// Send notification to employee about leave request status change
		notifyEmployeeAboutLeaveStatusChange(savedRequest, oldStatus, status);
		
		// Send notification to HR/Managers about leave request status change
		notifyHRAboutLeaveStatusChange(savedRequest, oldStatus, status);
		
		return savedRequest;
	}

	private void updateLeaveBalance(LeaveRequest request) {
		int currentYear = request.getStartDate().getYear();
		Optional<LeaveBalance> balance = leaveBalanceRepo.findByEmployeeIdAndLeaveTypeAndYear(
			request.getEmployee().getId(), request.getLeaveType(), currentYear);
		
		if (balance.isPresent()) {
			LeaveBalance lb = balance.get();
			lb.setUsedDays(lb.getUsedDays() + request.getDurationInDays());
			lb.setRemainingDays(lb.getTotalDays() - lb.getUsedDays());
			lb.setLastUpdated(LocalDate.now());
			leaveBalanceRepo.save(lb);
		}
	}

	private void restoreLeaveBalance(LeaveRequest request) {
		int currentYear = request.getStartDate().getYear();
		Optional<LeaveBalance> balance = leaveBalanceRepo.findByEmployeeIdAndLeaveTypeAndYear(
			request.getEmployee().getId(), request.getLeaveType(), currentYear);
		
		if (balance.isPresent()) {
			LeaveBalance lb = balance.get();
			lb.setUsedDays(Math.max(0, lb.getUsedDays() - request.getDurationInDays()));
			lb.setRemainingDays(lb.getTotalDays() - lb.getUsedDays());
			lb.setLastUpdated(LocalDate.now());
			leaveBalanceRepo.save(lb);
		}
	}

	public List<LeaveBalance> getLeaveBalances(Long employeeId) {
		return leaveBalanceRepo.findByEmployeeId(employeeId);
	}

	public List<LeaveBalance> getLeaveBalances(Long employeeId, int year) {
		return leaveBalanceRepo.findByEmployeeIdAndYear(employeeId, year);
	}

	public LeaveBalance createLeaveBalance(EmployeeProfile employee, LeaveBalance.LeaveType leaveType, int totalDays, int year) {
		LeaveBalance balance = new LeaveBalance(employee, leaveType, totalDays, year);
		return leaveBalanceRepo.save(balance);
	}

	public void updateLeaveBalance(LeaveBalance balance) {
		balance.setLastUpdated(LocalDate.now());
		leaveBalanceRepo.save(balance);
	}
	
	// Helper method to notify employee about leave request status change
	private void notifyEmployeeAboutLeaveStatusChange(LeaveRequest request, LeaveRequest.Status oldStatus, LeaveRequest.Status newStatus) {
		try {
			// Find the employee's user account
			Optional<UserAccount> employeeUser = userAccountRepository.findAll().stream()
				.filter(user -> user.getUsername().equals(request.getEmployee().getEmail().split("@")[0]))
				.findFirst();
			
			if (employeeUser.isPresent()) {
				String statusMessage = switch (newStatus) {
					case APPROVED -> "Your leave request has been approved";
					case REJECTED -> "Your leave request has been rejected";
					case CANCELLED -> "Your leave request has been cancelled";
					default -> "Your leave request status has been updated";
				};
				
				notificationService.createLeaveAlert(
					employeeUser.get(),
					"Leave Request Update",
					String.format("%s for %s (%s to %s). %s", 
						statusMessage, 
						request.getLeaveType().getDisplayName(),
						request.getStartDate(),
						request.getEndDate(),
						request.getComments() != null ? "Comments: " + request.getComments() : "")
				);
			}
		} catch (Exception e) {
			System.err.println("Failed to send leave notification to employee: " + e.getMessage());
		}
	}
	
	// Helper method to notify HR/Managers about leave request status change
	private void notifyHRAboutLeaveStatusChange(LeaveRequest request, LeaveRequest.Status oldStatus, LeaveRequest.Status newStatus) {
		try {
			// Get all HR and Manager users
			List<UserAccount> hrAndManagers = userAccountRepository.findAll().stream()
				.filter(user -> user.hasAnyRole("HR", "MANAGER", "ADMIN"))
				.toList();
			
			// Send notification to each HR/Manager
			for (UserAccount user : hrAndManagers) {
				notificationService.createApprovalAlert(
					user,
					"Leave Request Status Updated",
					String.format("Leave request for %s %s (%s) has been %s. %s", 
						request.getEmployee().getFirstName(),
						request.getEmployee().getLastName(),
						request.getLeaveType().getDisplayName(),
						newStatus.name().toLowerCase(),
						request.getComments() != null ? "Comments: " + request.getComments() : "")
				);
			}
		} catch (Exception e) {
			System.err.println("Failed to send leave notification to HR/Managers: " + e.getMessage());
		}
	}
}


