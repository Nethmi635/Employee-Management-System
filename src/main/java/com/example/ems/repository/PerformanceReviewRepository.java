package com.example.ems.repository;

import com.example.ems.domain.performance.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
	List<PerformanceReview> findByEmployeeId(Long employeeId);
}
