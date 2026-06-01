package com.example.ems.repository;

import com.example.ems.domain.employee.FullTimeEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FullTimeEmployeeRepository extends JpaRepository<FullTimeEmployee, Long> {}
