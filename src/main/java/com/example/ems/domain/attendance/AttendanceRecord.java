package com.example.ems.domain.attendance;

import com.example.ems.domain.employee.EmployeeProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AttendanceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private EmployeeProfile employee;

	private LocalDate date;
	private LocalDateTime checkInAt;
	private LocalDateTime checkOutAt;

	// Standard working hours per day (8 hours)
	private static final int STANDARD_HOURS_PER_DAY = 8;

	public boolean isComplete() {
		return checkInAt != null && checkOutAt != null;
	}

	public Duration getWorkingDuration() {
		if (!isComplete()) {
			return Duration.ZERO;
		}
		return Duration.between(checkInAt, checkOutAt);
	}

	public double getWorkingHours() {
		return getWorkingDuration().toMinutes() / 60.0;
	}

	public double getOvertimeHours() {
		double workingHours = getWorkingHours();
		return Math.max(0, workingHours - STANDARD_HOURS_PER_DAY);
	}

	public boolean hasOvertime() {
		return getOvertimeHours() > 0;
	}

	public boolean isPresent() {
		return checkInAt != null;
	}

	public boolean isAbsent() {
		return checkInAt == null;
	}

	public boolean isPartial() {
		return checkInAt != null && checkOutAt == null;
	}
}


