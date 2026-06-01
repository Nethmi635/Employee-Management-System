package com.example.ems.repository;

import com.example.ems.domain.employee.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
	List<EmployeeProfile> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String firstName, String lastName, String email);
	List<EmployeeProfile> findByActive(Boolean active);
	Optional<EmployeeProfile> findByEmail(String email);
	Optional<EmployeeProfile> findByFirstNameIgnoreCase(String firstName);
}


