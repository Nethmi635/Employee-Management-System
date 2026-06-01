package com.example.ems.controller;

import com.example.ems.domain.employee.ContractEmployee;
import com.example.ems.domain.employee.EmployeeDocument;
import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.employee.FullTimeEmployee;
import com.example.ems.service.EmployeeService;
import com.example.ems.service.EmployeeValidationService;
import com.example.ems.service.NotificationService;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.domain.user.UserAccount;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

	private final EmployeeService employeeService;
	private final EmployeeValidationService validationService;
	private final NotificationService notificationService;
	private final UserAccountRepository userAccountRepository;

	public EmployeeController(EmployeeService employeeService, EmployeeValidationService validationService, NotificationService notificationService, UserAccountRepository userAccountRepository) {
		this.employeeService = employeeService;
		this.validationService = validationService;
		this.notificationService = notificationService;
		this.userAccountRepository = userAccountRepository;
	}

	@GetMapping
	@PreAuthorize("hasPermission(null, 'EMPLOYEE_VIEW') or hasRole('HR') or hasRole('ADMIN')")
    public String list(@RequestParam(value = "q", required = false) String q,
                         @RequestParam(value = "active", required = false) Boolean active,
                         @RequestParam(value = "department", required = false) String department,
                         @RequestParam(value = "jobTitle", required = false) String jobTitle,
                         @RequestParam(value = "employmentType", required = false) String employmentType,
                         Model model) {
        model.addAttribute("employees", employeeService.listAll(q, active, department, jobTitle, employmentType));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("active", active);
        model.addAttribute("department", department == null ? "" : department);
        model.addAttribute("jobTitle", jobTitle == null ? "" : jobTitle);
        model.addAttribute("employmentType", employmentType == null ? "" : employmentType);
        return "employee/list";
    }

	@GetMapping("/new")
	@PreAuthorize("hasPermission(null, 'EMPLOYEE_CREATE') or hasRole('HR') or hasRole('ADMIN')")
	public String createForm(Model model) {
		model.addAttribute("fullTimeEmployee", new FullTimeEmployee());
		model.addAttribute("contractEmployee", new ContractEmployee());
		return "employee/new";
	}

	@PostMapping("/new/fulltime")
	@PreAuthorize("hasPermission(null, 'EMPLOYEE_CREATE') or hasRole('HR') or hasRole('ADMIN')")
	public String createFullTime(@Valid @ModelAttribute("fullTimeEmployee") FullTimeEmployee e, BindingResult br, Authentication auth, Model model) {
		// Add contract employee to model for form display
		model.addAttribute("contractEmployee", new ContractEmployee());
		
		if (br.hasErrors()) {
			return "employee/new";
		}
		
		// Custom business validation
		List<String> validationErrors = validationService.validateEmployee(e);
		if (!validationErrors.isEmpty()) {
			model.addAttribute("validationErrors", validationErrors);
			return "employee/new";
		}
		
		try {
			employeeService.saveFullTime(e);
			
			// Send system alert to all admins about new employee
			notifyAdminsAboutNewEmployee(e, "Full-time");
			
			return "redirect:/employee?success=fulltime";
		} catch (Exception ex) {
			model.addAttribute("error", "Failed to create employee: " + ex.getMessage());
			return "employee/new";
		}
	}

	@PostMapping("/new/contract")
	public String createContract(@Valid @ModelAttribute("contractEmployee") ContractEmployee e, BindingResult br, Authentication auth, Model model) {
		// Add full time employee to model for form display
		model.addAttribute("fullTimeEmployee", new FullTimeEmployee());
		
		if (br.hasErrors()) {
			return "employee/new";
		}
		
		// Custom business validation
		List<String> validationErrors = validationService.validateEmployee(e);
		if (!validationErrors.isEmpty()) {
			model.addAttribute("validationErrors", validationErrors);
			return "employee/new";
		}
		
		try {
			employeeService.saveContract(e);
			
			// Send system alert to all admins about new employee
			notifyAdminsAboutNewEmployee(e, "Contract");
			
			return "redirect:/employee?success=contract";
		} catch (Exception ex) {
			model.addAttribute("error", "Failed to create employee: " + ex.getMessage());
			return "employee/new";
		}
	}

	@GetMapping("/{id}")
	public String view(@PathVariable Long id, Model model) {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		model.addAttribute("employee", e);
		model.addAttribute("documents", employeeService.listDocuments(e));
		return "employee/view";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		model.addAttribute("employee", e);
		return "employee/edit";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable Long id,
						@RequestParam String firstName,
						@RequestParam String lastName,
						@RequestParam String email,
						@RequestParam String phone,
						@RequestParam String department,
						@RequestParam String jobTitle,
						@RequestParam(required = false, defaultValue = "false") boolean active,
						@RequestParam(required = false) MultipartFile photo) throws IOException {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		e.setFirstName(firstName);
		e.setLastName(lastName);
		e.setEmail(email);
		e.setPhone(phone);
		e.setDepartment(department);
		e.setJobTitle(jobTitle);
		e.setActive(active);
		if (photo != null && !photo.isEmpty()) {
			String filename = employeeService.storeProfilePhoto(photo);
			e.setPhotoFilename(filename);
		}
		employeeService.save(e);
		return "redirect:/employee";
	}

	@PostMapping("/{id}/deactivate")
	public String deactivate(@PathVariable Long id) {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		e.setActive(false);
		employeeService.save(e);
		return "redirect:/employee/" + id;
	}

	@PostMapping("/{id}/activate")
	public String activate(@PathVariable Long id) {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		e.setActive(true);
		employeeService.save(e);
		return "redirect:/employee/" + id;
	}

	@PostMapping("/{id}/documents")
	public String uploadDocument(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		if (!file.isEmpty()) {
			employeeService.storeDocument(e, file);
		}
		return "redirect:/employee/" + id;
	}

	@PostMapping("/{id}/documents/{docId}/delete")
	public String deleteDocument(@PathVariable Long id, @PathVariable Long docId) throws IOException {
		EmployeeProfile e = employeeService.findById(id).orElseThrow();
		for (EmployeeDocument d : employeeService.listDocuments(e)) {
			if (d.getId().equals(docId)) {
				employeeService.deleteDocument(d);
				break;
			}
		}
		return "redirect:/employee/" + id;
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id) {
		employeeService.delete(id);
		return "redirect:/employee";
	}
	
	// Helper method to notify admins about new employee creation
	private void notifyAdminsAboutNewEmployee(EmployeeProfile employee, String employeeType) {
		try {
			// Get all admin users
			List<UserAccount> adminUsers = userAccountRepository.findAll().stream()
				.filter(user -> user.hasRole("ADMIN"))
				.toList();
			
			// Send notification to each admin
			for (UserAccount admin : adminUsers) {
				notificationService.createSystemAlert(
					admin,
					"New Employee Added",
					String.format("A new %s employee '%s %s' has been added to the system in %s department.", 
						employeeType, employee.getFirstName(), employee.getLastName(), employee.getDepartment())
				);
			}
		} catch (Exception e) {
			// Log error but don't break the employee creation process
			System.err.println("Failed to send notification about new employee: " + e.getMessage());
		}
	}
}


