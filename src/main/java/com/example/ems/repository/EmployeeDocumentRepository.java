package com.example.ems.repository;

import com.example.ems.domain.employee.EmployeeDocument;
import com.example.ems.domain.employee.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
	List<EmployeeDocument> findByEmployee(EmployeeProfile employee);
}


