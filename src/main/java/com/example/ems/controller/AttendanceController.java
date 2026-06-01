package com.example.ems.controller;

import com.example.ems.domain.attendance.AttendanceRecord;
import com.example.ems.repository.AttendanceRecordRepository;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

	private final AttendanceRecordRepository attendanceRepo;
	private final EmployeeProfileRepository employeeRepo;

	public AttendanceController(AttendanceRecordRepository attendanceRepo, EmployeeProfileRepository employeeRepo) {
		this.attendanceRepo = attendanceRepo;
		this.employeeRepo = employeeRepo;
	}

	@GetMapping
	public String list(@RequestParam(required = false) Long employeeId, Model model) {
		model.addAttribute("records", employeeId == null ? attendanceRepo.findAll() : attendanceRepo.findByEmployeeId(employeeId));
		model.addAttribute("employees", employeeRepo.findAll());
		model.addAttribute("employeeId", employeeId);
		return "attendance/list";
	}

	@PostMapping("/checkin")
	public String checkIn(@RequestParam Long employeeId) {
		AttendanceRecord r = new AttendanceRecord();
		r.setEmployee(employeeRepo.findById(employeeId).orElseThrow());
		r.setDate(LocalDate.now());
		r.setCheckInAt(LocalDateTime.now());
		attendanceRepo.save(r);
		return "redirect:/attendance?employeeId=" + employeeId;
	}

	@PostMapping("/checkout/{id}")
	public String checkOut(@PathVariable Long id) {
		AttendanceRecord r = attendanceRepo.findById(id).orElseThrow();
		r.setCheckOutAt(LocalDateTime.now());
		attendanceRepo.save(r);
		return "redirect:/attendance";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, @RequestParam(required = false) Long employeeId) {
		attendanceRepo.deleteById(id);
		return "redirect:/attendance" + (employeeId != null ? ("?employeeId=" + employeeId) : "");
	}

	@GetMapping(value = "/export.csv")
	public ResponseEntity<String> exportCsv(@RequestParam(required = false) Long employeeId) {
		var list = employeeId == null ? attendanceRepo.findAll() : attendanceRepo.findByEmployeeId(employeeId);
		String csv = "ID,Employee,Date,CheckIn,CheckOut\n" + list.stream().map(r ->
			r.getId() + "," +
			r.getEmployee().getFirstName() + " " + r.getEmployee().getLastName() + "," +
			r.getDate() + "," +
			r.getCheckInAt() + "," +
			r.getCheckOutAt()
		).collect(Collectors.joining("\n"));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.csv");
		return ResponseEntity.ok().headers(headers).body(csv);
	}

	@GetMapping(value = "/export.xlsx")
	public ResponseEntity<byte[]> exportXlsx(@RequestParam(required = false) Long employeeId) throws java.io.IOException {
		var list = employeeId == null ? attendanceRepo.findAll() : attendanceRepo.findByEmployeeId(employeeId);
		try (Workbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Attendance");
			Row header = sheet.createRow(0);
			String[] cols = {"ID","Employee","Date","CheckIn","CheckOut"};
			for (int i=0;i<cols.length;i++){ header.createCell(i).setCellValue(cols[i]); }
			int r=1;
			for (var rec : list) {
				Row row = sheet.createRow(r++);
				row.createCell(0).setCellValue(rec.getId());
				row.createCell(1).setCellValue(rec.getEmployee().getFirstName()+" "+rec.getEmployee().getLastName());
				row.createCell(2).setCellValue(String.valueOf(rec.getDate()));
				row.createCell(3).setCellValue(rec.getCheckInAt()==null?"":String.valueOf(rec.getCheckInAt()));
				row.createCell(4).setCellValue(rec.getCheckOutAt()==null?"":String.valueOf(rec.getCheckOutAt()));
			}
			for (int i=0;i<cols.length;i++){ sheet.autoSizeColumn(i); }
			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			wb.write(out);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.xlsx");
			return ResponseEntity.ok().headers(headers).body(out.toByteArray());
		}
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("employees", employeeRepo.findAll());
		return "attendance/new";
	}

	@PostMapping("/new")
	public String create(@RequestParam Long employeeId,
					  @RequestParam String date,
					  @RequestParam(required = false) String checkInAt,
					  @RequestParam(required = false) String checkOutAt) {
		AttendanceRecord r = new AttendanceRecord();
		r.setEmployee(employeeRepo.findById(employeeId).orElseThrow());
		r.setDate(LocalDate.parse(date));
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		r.setCheckInAt((checkInAt == null || checkInAt.isBlank()) ? null : LocalDateTime.parse(checkInAt, fmt));
		r.setCheckOutAt((checkOutAt == null || checkOutAt.isBlank()) ? null : LocalDateTime.parse(checkOutAt, fmt));
		attendanceRepo.save(r);
		return "redirect:/attendance";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		AttendanceRecord r = attendanceRepo.findById(id).orElseThrow();
		model.addAttribute("record", r);
		return "attendance/edit";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable Long id,
					 @RequestParam String date,
					 @RequestParam(required = false) String checkInAt,
					 @RequestParam(required = false) String checkOutAt) {
		AttendanceRecord r = attendanceRepo.findById(id).orElseThrow();
		r.setDate(LocalDate.parse(date));
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		r.setCheckInAt((checkInAt == null || checkInAt.isBlank()) ? null : LocalDateTime.parse(checkInAt, fmt));
		r.setCheckOutAt((checkOutAt == null || checkOutAt.isBlank()) ? null : LocalDateTime.parse(checkOutAt, fmt));
		attendanceRepo.save(r);
		return "redirect:/attendance";
	}
}


