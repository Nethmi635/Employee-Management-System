package com.example.ems.repository;

import com.example.ems.domain.attendance.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
	List<AttendanceRecord> findByEmployeeId(Long employeeId);
	
	List<AttendanceRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);
	
	List<AttendanceRecord> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
	
	@Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.id = :employeeId AND ar.date = :date")
	List<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
	
	@Query("SELECT ar FROM AttendanceRecord ar WHERE ar.date = :date")
	List<AttendanceRecord> findByDate(LocalDate date);
}
