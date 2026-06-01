package com.example.ems.repository;

import com.example.ems.domain.shift.ShiftSwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {
    
    List<ShiftSwapRequest> findByRequesterId(Long requesterId);
    
    List<ShiftSwapRequest> findByTargetEmployeeId(Long targetEmployeeId);
    
    List<ShiftSwapRequest> findByStatus(ShiftSwapRequest.Status status);
    
    List<ShiftSwapRequest> findByRequesterIdAndStatus(Long requesterId, ShiftSwapRequest.Status status);
    
    List<ShiftSwapRequest> findByTargetEmployeeIdAndStatus(Long targetEmployeeId, ShiftSwapRequest.Status status);
}
