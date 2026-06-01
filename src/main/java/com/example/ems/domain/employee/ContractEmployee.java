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
@DiscriminatorValue("CONTRACT")
public class ContractEmployee extends EmployeeProfile {

	@NotNull(message = "Hourly rate is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
	private Double hourlyRate;
}


