package com.example.ems.domain.employee;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("FULL_TIME")
public class FullTimeEmployee extends EmployeeProfile {

	@NotNull(message = "Annual salary is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Annual salary must be greater than 0")
	private Double annualSalary;
}


