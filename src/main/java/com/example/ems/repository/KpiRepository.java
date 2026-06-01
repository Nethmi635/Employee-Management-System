package com.example.ems.repository;

import com.example.ems.domain.performance.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KpiRepository extends JpaRepository<Kpi, Long> {
	List<Kpi> findByEmployeeId(Long employeeId);
}


