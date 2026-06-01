package com.example.ems.domain.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class EmployeeDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private EmployeeProfile employee;

	@NotBlank
	private String originalFilename;

	@NotBlank
	@Column(unique = true)
	private String storedFilename;

	@NotBlank
	private String contentType;
}


