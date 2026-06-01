package com.example.ems.repository;

import com.example.ems.domain.leave.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    
    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);
    
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(Long employeeId, LeaveBalance.LeaveType leaveType, int year);
    
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year AND lb.remainingDays > 0")
    List<LeaveBalance> findAvailableBalances(@Param("employeeId") Long employeeId, @Param("year") int year);
    
    @Query("SELECT SUM(lb.remainingDays) FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year")
    Integer getTotalRemainingDays(@Param("employeeId") Long employeeId, @Param("year") int year);
}
