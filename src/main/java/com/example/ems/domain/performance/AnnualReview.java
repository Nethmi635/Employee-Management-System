package com.example.ems.domain.performance;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("ANNUAL")
public class AnnualReview extends PerformanceReview {

	@Column(name = "review_year")
	private Integer year;
}


