package com.example.ems.domain.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "employment_type")
public abstract class EmployeeProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "First name is required")
	@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
	@Column(name = "first_name")
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
	@Column(name = "last_name")
	private String lastName;

	@Email(message = "Please provide a valid email address")
	@NotBlank(message = "Email is required")
	@Size(max = 100, message = "Email must not exceed 100 characters")
	@Column(unique = true)
	private String email;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,15}$", message = "Please provide a valid phone number")
	private String phone;

	@NotBlank(message = "Department is required")
	@Size(min = 2, max = 50, message = "Department must be between 2 and 50 characters")
	private String department;

	@NotBlank(message = "Job title is required")
	@Size(min = 2, max = 50, message = "Job title must be between 2 and 50 characters")
	@Column(name = "job_title")
	private String jobTitle;

	@NotNull
	private Boolean active = true;

	// Optional profile photo filename stored under uploads/
	@Column(name = "photo_filename")
	private String photoFilename;
}


