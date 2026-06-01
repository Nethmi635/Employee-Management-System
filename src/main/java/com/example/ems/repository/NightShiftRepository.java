package com.example.ems.repository;

import com.example.ems.domain.shift.NightShift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NightShiftRepository extends JpaRepository<NightShift, Long> {}
