package com.example.ems.domain.performance;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("QUARTERLY")
public class QuarterlyReview extends PerformanceReview {

	private String quarter; // e.g., Q1 2025
}


