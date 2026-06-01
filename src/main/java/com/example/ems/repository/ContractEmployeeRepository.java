package com.example.ems.repository;

import com.example.ems.domain.employee.ContractEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractEmployeeRepository extends JpaRepository<ContractEmployee, Long> {}
