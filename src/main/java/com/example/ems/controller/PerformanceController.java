package com.example.ems.controller;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.performance.AnnualReview;
import com.example.ems.domain.performance.Kpi;
import com.example.ems.domain.performance.PerformanceReview;
import com.example.ems.domain.performance.QuarterlyReview;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.service.PerformanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final EmployeeProfileRepository employeeRepo;

    public PerformanceController(PerformanceService performanceService, EmployeeProfileRepository employeeRepo) {
        this.performanceService = performanceService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long employeeId, Model model) {
        List<PerformanceReview> reviews = employeeId == null ? 
            performanceService.getAllReviews() : performanceService.getReviewsByEmployee(employeeId);
        List<Kpi> kpis = employeeId == null ? 
            performanceService.getAllKpis() : performanceService.getKpisByEmployee(employeeId);
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("employees", employeeRepo.findAll());
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("kpis", kpis);
        return "performance/list";
    }

	@PostMapping("/quarterly")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
	public String addQuarterly(@RequestParam Long employeeId,
							@RequestParam String quarter,
							@RequestParam int rating,
							@RequestParam String feedback) {
		EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
		performanceService.createQuarterlyReview(employee, quarter, rating, feedback);
		return "redirect:/performance";
	}

	@PostMapping("/annual")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
	public String addAnnual(@RequestParam Long employeeId,
						   @RequestParam int year,
						   @RequestParam int rating,
						   @RequestParam String feedback) {
		EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
		performanceService.createAnnualReview(employee, year, rating, feedback);
		return "redirect:/performance";
	}

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String edit(@PathVariable Long id, @RequestParam int rating, @RequestParam String feedback) {
        performanceService.updateReview(id, rating, feedback);
        return "redirect:/performance";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String delete(@PathVariable Long id) {
        performanceService.deleteReview(id);
        return "redirect:/performance";
    }

    @PostMapping("/kpi")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String addKpi(@RequestParam Long employeeId,
                         @RequestParam String name,
                         @RequestParam String description,
                         @RequestParam Double targetValue,
                         @RequestParam(required = false) Double currentValue) {
        EmployeeProfile employee = employeeRepo.findById(employeeId).orElseThrow();
        performanceService.createKpi(employee, name, description, targetValue, currentValue);
        return "redirect:/performance?employeeId=" + employeeId;
    }

    @PostMapping("/kpi/{id}/edit")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String editKpi(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam String description,
                         @RequestParam Double targetValue,
                         @RequestParam(required = false) Double currentValue) {
        performanceService.updateKpi(id, name, description, targetValue, currentValue);
        return "redirect:/performance";
    }

    @PostMapping("/kpi/{id}/delete")
    @PreAuthorize("hasAnyRole('MANAGER','HR','ADMIN')")
    public String deleteKpi(@PathVariable Long id, @RequestParam(required = false) Long employeeId) {
        performanceService.deleteKpi(id);
        return "redirect:/performance" + (employeeId == null ? "" : ("?employeeId=" + employeeId));
    }

    @GetMapping("/export.csv")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) Long employeeId) {
        var list = employeeId == null ? performanceService.getAllReviews() : performanceService.getReviewsByEmployee(employeeId);
        String csv = "ID,Type,Employee,Date,Rating,Feedback\n" + list.stream().map(r ->
                r.getId() + "," + r.getClass().getSimpleName() + "," +
                        r.getEmployee().getFirstName() + " " + r.getEmployee().getLastName() + "," +
                        r.getReviewDate() + "," + r.getRating() + "," +
                        (r.getFeedback() == null ? "" : r.getFeedback().replace(","," "))
        ).collect(java.util.stream.Collectors.joining("\n"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportXlsx(@RequestParam(required = false) Long employeeId) throws java.io.IOException {
        var list = employeeId == null ? performanceService.getAllReviews() : performanceService.getReviewsByEmployee(employeeId);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Performance");
            Row header = sheet.createRow(0);
            String[] cols = {"ID","Type","Employee","Date","Rating","Feedback"};
            for (int i=0;i<cols.length;i++){ header.createCell(i).setCellValue(cols[i]); }
            int r=1;
            for (var rec : list) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(rec.getId());
                row.createCell(1).setCellValue(rec.getClass().getSimpleName());
                row.createCell(2).setCellValue(rec.getEmployee().getFirstName()+" "+rec.getEmployee().getLastName());
                row.createCell(3).setCellValue(String.valueOf(rec.getReviewDate()));
                row.createCell(4).setCellValue(rec.getRating()==null?0:rec.getRating());
                row.createCell(5).setCellValue(rec.getFeedback()==null?"":rec.getFeedback());
            }
            for (int i=0;i<cols.length;i++){ sheet.autoSizeColumn(i); }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            wb.write(out);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance.xlsx");
            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
        }
    }
}


