package com.example.ems.repository;

import com.example.ems.domain.leave.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	List<LeaveRequest> findByEmployeeId(Long employeeId);
	List<LeaveRequest> findByStatus(LeaveRequest.Status status);
	List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.Status status);

	boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long employeeId, List<LeaveRequest.Status> statuses, java.time.LocalDate endDate, java.time.LocalDate startDate);
}
