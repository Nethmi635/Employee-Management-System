package com.example.ems.domain.performance;

import com.example.ems.domain.employee.EmployeeProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Kpi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private EmployeeProfile employee;

	private String name;
	private String description;
	private Double targetValue;
	private Double currentValue;
	private LocalDate createdDate = LocalDate.now();
	private LocalDate lastUpdated = LocalDate.now();

	public double getProgressPercentage() {
		if (targetValue == null || targetValue == 0) return 0.0;
		return (currentValue / targetValue) * 100.0;
	}

	public String getStatus() {
		double progress = getProgressPercentage();
		if (progress >= 100) return "Achieved";
		if (progress >= 80) return "On Track";
		if (progress >= 50) return "Needs Improvement";
		return "At Risk";
	}

	public boolean isAchieved() {
		return getProgressPercentage() >= 100.0;
	}

	public double getRemainingValue() {
		if (targetValue == null || currentValue == null) return 0.0;
		return Math.max(0, targetValue - currentValue);
	}
}


