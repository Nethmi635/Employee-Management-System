package com.example.ems.repository;

import com.example.ems.domain.shift.DayShift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayShiftRepository extends JpaRepository<DayShift, Long> {}
