package com.example.ems.repository;

import com.example.ems.domain.shift.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
	List<Shift> findByEmployeeId(Long employeeId);
	List<Shift> findByShiftDate(LocalDate date);
	List<Shift> findByEmployeeIdAndShiftDate(Long employeeId, java.time.LocalDate date);
}
