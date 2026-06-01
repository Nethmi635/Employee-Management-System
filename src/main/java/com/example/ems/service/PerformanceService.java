package com.example.ems.service;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.performance.AnnualReview;
import com.example.ems.domain.performance.Kpi;
import com.example.ems.domain.performance.PerformanceReview;
import com.example.ems.domain.performance.QuarterlyReview;
import com.example.ems.repository.KpiRepository;
import com.example.ems.repository.PerformanceReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceService {

    private final PerformanceReviewRepository reviewRepo;
    private final KpiRepository kpiRepo;

    public PerformanceService(PerformanceReviewRepository reviewRepo, KpiRepository kpiRepo) {
        this.reviewRepo = reviewRepo;
        this.kpiRepo = kpiRepo;
    }

    // Performance Review Methods
    public List<PerformanceReview> getAllReviews() {
        return reviewRepo.findAll();
    }

    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return reviewRepo.findByEmployeeId(employeeId);
    }

    public List<QuarterlyReview> getQuarterlyReviews(Long employeeId) {
        return reviewRepo.findByEmployeeId(employeeId).stream()
            .filter(r -> r instanceof QuarterlyReview)
            .map(r -> (QuarterlyReview) r)
            .collect(Collectors.toList());
    }

    public List<AnnualReview> getAnnualReviews(Long employeeId) {
        return reviewRepo.findByEmployeeId(employeeId).stream()
            .filter(r -> r instanceof AnnualReview)
            .map(r -> (AnnualReview) r)
            .collect(Collectors.toList());
    }

    public PerformanceReview createQuarterlyReview(EmployeeProfile employee, String quarter, int rating, String feedback) {
        QuarterlyReview review = new QuarterlyReview();
        review.setEmployee(employee);
        review.setQuarter(quarter);
        review.setRating(rating);
        review.setFeedback(feedback);
        review.setReviewDate(LocalDate.now());
        return reviewRepo.save(review);
    }

    public PerformanceReview createAnnualReview(EmployeeProfile employee, int year, int rating, String feedback) {
        AnnualReview review = new AnnualReview();
        review.setEmployee(employee);
        review.setYear(year);
        review.setRating(rating);
        review.setFeedback(feedback);
        review.setReviewDate(LocalDate.now());
        return reviewRepo.save(review);
    }

    public PerformanceReview updateReview(Long id, int rating, String feedback) {
        PerformanceReview review = reviewRepo.findById(id).orElseThrow();
        review.setRating(rating);
        review.setFeedback(feedback);
        return reviewRepo.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepo.deleteById(id);
    }

    // KPI Methods
    public List<Kpi> getAllKpis() {
        return kpiRepo.findAll();
    }

    public List<Kpi> getKpisByEmployee(Long employeeId) {
        return kpiRepo.findByEmployeeId(employeeId);
    }

    public Kpi createKpi(EmployeeProfile employee, String name, String description, Double targetValue, Double currentValue) {
        Kpi kpi = new Kpi();
        kpi.setEmployee(employee);
        kpi.setName(name);
        kpi.setDescription(description);
        kpi.setTargetValue(targetValue);
        kpi.setCurrentValue(currentValue == null ? 0.0 : currentValue);
        return kpiRepo.save(kpi);
    }

    public Kpi updateKpi(Long id, String name, String description, Double targetValue, Double currentValue) {
        Kpi kpi = kpiRepo.findById(id).orElseThrow();
        kpi.setName(name);
        kpi.setDescription(description);
        kpi.setTargetValue(targetValue);
        kpi.setCurrentValue(currentValue == null ? 0.0 : currentValue);
        return kpiRepo.save(kpi);
    }

    public void deleteKpi(Long id) {
        kpiRepo.deleteById(id);
    }

    // Analytics and Statistics
    public Map<String, Object> getPerformanceStatistics(Long employeeId) {
        List<PerformanceReview> reviews = employeeId != null ? 
            getReviewsByEmployee(employeeId) : getAllReviews();
        List<Kpi> kpis = employeeId != null ? 
            getKpisByEmployee(employeeId) : getAllKpis();

        double averageRating = reviews.stream()
            .mapToInt(PerformanceReview::getRating)
            .average()
            .orElse(0.0);

        long totalReviews = reviews.size();
        long quarterlyReviews = reviews.stream()
            .filter(r -> r instanceof QuarterlyReview)
            .count();
        long annualReviews = reviews.stream()
            .filter(r -> r instanceof AnnualReview)
            .count();

        double averageKpiProgress = kpis.stream()
            .mapToDouble(this::calculateKpiProgress)
            .average()
            .orElse(0.0);

        long totalKpis = kpis.size();
        long achievedKpis = kpis.stream()
            .filter(kpi -> calculateKpiProgress(kpi) >= 100.0)
            .count();

        return Map.of(
            "averageRating", averageRating,
            "totalReviews", totalReviews,
            "quarterlyReviews", quarterlyReviews,
            "annualReviews", annualReviews,
            "averageKpiProgress", averageKpiProgress,
            "totalKpis", totalKpis,
            "achievedKpis", achievedKpis
        );
    }

    public double calculateKpiProgress(Kpi kpi) {
        if (kpi.getTargetValue() == 0) return 0.0;
        return (kpi.getCurrentValue() / kpi.getTargetValue()) * 100.0;
    }

    public String getKpiStatus(Kpi kpi) {
        double progress = calculateKpiProgress(kpi);
        if (progress >= 100) return "Achieved";
        if (progress >= 80) return "On Track";
        if (progress >= 50) return "Needs Improvement";
        return "At Risk";
    }

    public List<Map<String, Object>> getTopPerformers(int limit) {
        return getAllReviews().stream()
            .collect(Collectors.groupingBy(
                PerformanceReview::getEmployee,
                Collectors.averagingInt(PerformanceReview::getRating)
            ))
            .entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(entry -> Map.of(
                "employee", entry.getKey(),
                "averageRating", entry.getValue()
            ))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getKpiSummary() {
        return getAllKpis().stream()
            .map(kpi -> Map.of(
                "kpi", kpi,
                "progress", calculateKpiProgress(kpi),
                "status", getKpiStatus(kpi)
            ))
            .collect(Collectors.toList());
    }
}
