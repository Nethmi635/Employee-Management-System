package com.example.ems.domain.leave;

import com.example.ems.domain.employee.EmployeeProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class LeaveRequest {

	public enum Status { PENDING, APPROVED, REJECTED, CANCELLED }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private EmployeeProfile employee;

	private LocalDate startDate;
	private LocalDate endDate;
	private String reason;

	@Enumerated(EnumType.STRING)
	private Status status = Status.PENDING;

	@Enumerated(EnumType.STRING)
	private LeaveBalance.LeaveType leaveType = LeaveBalance.LeaveType.PERSONAL;

	private LocalDate submittedAt = LocalDate.now();
	private String comments;

	public int getDurationInDays() {
		return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}


