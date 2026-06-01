package com.example.ems.config;

import com.example.ems.domain.attendance.AttendanceRecord;
import com.example.ems.domain.employee.ContractEmployee;
import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.employee.FullTimeEmployee;
import com.example.ems.domain.leave.LeaveBalance;
import com.example.ems.domain.leave.LeaveRequest;
import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.performance.AnnualReview;
import com.example.ems.domain.performance.Kpi;
import com.example.ems.domain.performance.QuarterlyReview;
import com.example.ems.domain.shift.DayShift;
import com.example.ems.domain.shift.NightShift;
import com.example.ems.domain.user.*;
import com.example.ems.repository.*;
import com.example.ems.service.PermissionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class DataLoader {

	private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

	@Bean
	CommandLineRunner seedUsersAndRoles(RoleRepository roleRepo, UserAccountRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			if (roleRepo.count() == 0) {
				for (String name : List.of("ADMIN", "HR", "MANAGER", "EMPLOYEE")) {
					Role r = new Role();
					r.setName(name);
					roleRepo.save(r);
				}
			}
			if (userRepo.count() == 0) {
				Role admin = roleRepo.findByName("ADMIN").orElseThrow();
				Role hr = roleRepo.findByName("HR").orElseThrow();
				Role manager = roleRepo.findByName("MANAGER").orElseThrow();
				Role employee = roleRepo.findByName("EMPLOYEE").orElseThrow();

				AdminUser a = new AdminUser("db-admin", encoder.encode("admin123"), true); a.getRoles().add(admin); userRepo.save(a);
				ManagerUser h = new ManagerUser("db-hr", encoder.encode("hr123"), true); h.getRoles().add(hr); userRepo.save(h);
				ManagerUser m = new ManagerUser("db-manager", encoder.encode("manager123"), true); m.getRoles().add(manager); userRepo.save(m);
				EmployeeUser e = new EmployeeUser("db-employee", encoder.encode("emp123"), true); e.getRoles().add(employee); userRepo.save(e);
				
				// Additional sample users
				AdminUser dinaya = new AdminUser("dinaya", encoder.encode("dinaya123"), true); dinaya.getRoles().add(admin); userRepo.save(dinaya);
				ManagerUser sadeni = new ManagerUser("sadeni", encoder.encode("sadeni123"), true); sadeni.getRoles().add(hr); userRepo.save(sadeni);
				ManagerUser nethmi = new ManagerUser("nethmi", encoder.encode("nethmi123"), true); nethmi.getRoles().add(manager); userRepo.save(nethmi);
				EmployeeUser tharushi = new EmployeeUser("tharushi", encoder.encode("tharushi123"), true); tharushi.getRoles().add(employee); userRepo.save(tharushi);
				EmployeeUser hiruni = new EmployeeUser("hiruni", encoder.encode("hiruni123"), true); hiruni.getRoles().add(employee); userRepo.save(hiruni);
			}

			// Ensure in-memory Spring Security users also exist in DB for app features that rely on UserAccount
			try {
				Role adminRole = roleRepo.findByName("ADMIN").orElseThrow();
				Role hrRole = roleRepo.findByName("HR").orElseThrow();
				Role managerRole = roleRepo.findByName("MANAGER").orElseThrow();
				Role employeeRole = roleRepo.findByName("EMPLOYEE").orElseThrow();

				if (userRepo.findByUsername("admin").isEmpty()) {
					AdminUser u = new AdminUser("admin", encoder.encode("admin123"), true);
					u.getRoles().add(adminRole);
					userRepo.save(u);
				}
				if (userRepo.findByUsername("hr").isEmpty()) {
					ManagerUser u = new ManagerUser("hr", encoder.encode("hr123"), true);
					u.getRoles().add(hrRole);
					userRepo.save(u);
				}
				if (userRepo.findByUsername("manager").isEmpty()) {
					ManagerUser u = new ManagerUser("manager", encoder.encode("manager123"), true);
					u.getRoles().add(managerRole);
					userRepo.save(u);
				}
				if (userRepo.findByUsername("employee").isEmpty()) {
					EmployeeUser u = new EmployeeUser("employee", encoder.encode("emp123"), true);
					u.getRoles().add(employeeRole);
					userRepo.save(u);
				}
			} catch (Exception e) {
				log.warn("Could not ensure in-memory users exist in DB: {}", e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner seedEmployees(FullTimeEmployeeRepository fullTimeRepo, ContractEmployeeRepository contractRepo) {
		return args -> {
			if (fullTimeRepo.count() == 0) {
				// Create employee profiles linked to user accounts
				FullTimeEmployee adminEmp = new FullTimeEmployee();
				adminEmp.setFirstName("Admin");
				adminEmp.setLastName("User");
				adminEmp.setEmail("db-admin@example.com");
				adminEmp.setPhone("1234567890");
				adminEmp.setDepartment("IT");
				adminEmp.setJobTitle("System Administrator");
				adminEmp.setAnnualSalary(90000.0);
				fullTimeRepo.save(adminEmp);

				FullTimeEmployee hrEmp = new FullTimeEmployee();
				hrEmp.setFirstName("HR");
				hrEmp.setLastName("Manager");
				hrEmp.setEmail("db-hr@example.com");
				hrEmp.setPhone("1234567891");
				hrEmp.setDepartment("Human Resources");
				hrEmp.setJobTitle("HR Manager");
				hrEmp.setAnnualSalary(75000.0);
				fullTimeRepo.save(hrEmp);

				FullTimeEmployee managerEmp = new FullTimeEmployee();
				managerEmp.setFirstName("Manager");
				managerEmp.setLastName("User");
				managerEmp.setEmail("db-manager@example.com");
				managerEmp.setPhone("1234567892");
				managerEmp.setDepartment("Operations");
				managerEmp.setJobTitle("Operations Manager");
				managerEmp.setAnnualSalary(80000.0);
				fullTimeRepo.save(managerEmp);

				FullTimeEmployee employeeEmp = new FullTimeEmployee();
				employeeEmp.setFirstName("Employee");
				employeeEmp.setLastName("User");
				employeeEmp.setEmail("db-employee@example.com");
				employeeEmp.setPhone("1234567893");
				employeeEmp.setDepartment("IT");
				employeeEmp.setJobTitle("Software Developer");
				employeeEmp.setAnnualSalary(60000.0);
				fullTimeRepo.save(employeeEmp);

				// Additional sample employees
				FullTimeEmployee dinayaEmp = new FullTimeEmployee();
				dinayaEmp.setFirstName("Dinaya");
				dinayaEmp.setLastName("Perera");
				dinayaEmp.setEmail("dinaya@example.com");
				dinayaEmp.setPhone("1234567894");
				dinayaEmp.setDepartment("IT");
				dinayaEmp.setJobTitle("Senior Developer");
				dinayaEmp.setAnnualSalary(85000.0);
				fullTimeRepo.save(dinayaEmp);

				FullTimeEmployee sadeniEmp = new FullTimeEmployee();
				sadeniEmp.setFirstName("Sadeni");
				sadeniEmp.setLastName("Fernando");
				sadeniEmp.setEmail("sadeni@example.com");
				sadeniEmp.setPhone("1234567895");
				sadeniEmp.setDepartment("Human Resources");
				sadeniEmp.setJobTitle("HR Specialist");
				sadeniEmp.setAnnualSalary(65000.0);
				fullTimeRepo.save(sadeniEmp);

				FullTimeEmployee nethmiEmp = new FullTimeEmployee();
				nethmiEmp.setFirstName("Nethmi");
				nethmiEmp.setLastName("Silva");
				nethmiEmp.setEmail("nethmi@example.com");
				nethmiEmp.setPhone("1234567896");
				nethmiEmp.setDepartment("Operations");
				nethmiEmp.setJobTitle("Team Lead");
				nethmiEmp.setAnnualSalary(70000.0);
				fullTimeRepo.save(nethmiEmp);

				FullTimeEmployee tharushiEmp = new FullTimeEmployee();
				tharushiEmp.setFirstName("Tharushi");
				tharushiEmp.setLastName("Jayawardena");
				tharushiEmp.setEmail("tharushi@example.com");
				tharushiEmp.setPhone("1234567897");
				tharushiEmp.setDepartment("IT");
				tharushiEmp.setJobTitle("Junior Developer");
				tharushiEmp.setAnnualSalary(50000.0);
				fullTimeRepo.save(tharushiEmp);

				FullTimeEmployee hiruniEmp = new FullTimeEmployee();
				hiruniEmp.setFirstName("Hiruni");
				hiruniEmp.setLastName("Wickramasinghe");
				hiruniEmp.setEmail("hiruni@example.com");
				hiruniEmp.setPhone("1234567898");
				hiruniEmp.setDepartment("Marketing");
				hiruniEmp.setJobTitle("Marketing Coordinator");
				hiruniEmp.setAnnualSalary(55000.0);
				fullTimeRepo.save(hiruniEmp);
			}
			if (contractRepo.count() == 0) {
				ContractEmployee c = new ContractEmployee();
				c.setFirstName("Sara");
				c.setLastName("Lee");
				c.setEmail("sara.lee@example.com");
				c.setPhone("5551112222");
				c.setDepartment("Operations");
				c.setJobTitle("Analyst");
				c.setHourlyRate(45.0);
				contractRepo.save(c);
			}
		};
	}

	@Bean
	CommandLineRunner seedAttendance(AttendanceRecordRepository attendanceRepo, EmployeeProfileRepository employeeRepo) {
		return args -> {
			if (attendanceRepo.count() == 0) {
				EmployeeProfile emp = employeeRepo.findAll().stream().findFirst().orElse(null);
				if (emp != null) {
					AttendanceRecord r = new AttendanceRecord();
					r.setEmployee(emp);
					r.setDate(LocalDate.now());
					r.setCheckInAt(LocalDateTime.now().withHour(9).withMinute(0));
					r.setCheckOutAt(LocalDateTime.now().withHour(17).withMinute(0));
					attendanceRepo.save(r);
				}
			}
		};
	}

	@Bean
	CommandLineRunner seedLeave(LeaveRequestRepository leaveRepo, EmployeeProfileRepository employeeRepo) {
		return args -> {
			if (leaveRepo.count() == 0) {
				EmployeeProfile emp = employeeRepo.findAll().stream().findFirst().orElse(null);
				if (emp != null) {
					LeaveRequest p = new LeaveRequest();
					p.setEmployee(emp);
					p.setStartDate(LocalDate.now().plusDays(7));
					p.setEndDate(LocalDate.now().plusDays(9));
					p.setReason("Vacation");
					leaveRepo.save(p);

					LeaveRequest a = new LeaveRequest();
					a.setEmployee(emp);
					a.setStartDate(LocalDate.now().minusDays(14));
					a.setEndDate(LocalDate.now().minusDays(12));
					a.setReason("Personal");
					a.setStatus(LeaveRequest.Status.APPROVED);
					leaveRepo.save(a);
				}
			}
		};
	}

	@Bean
	CommandLineRunner seedPerformance(PerformanceReviewRepository perfRepo, EmployeeProfileRepository employeeRepo, KpiRepository kpiRepo) {
		return args -> {
			if (perfRepo.count() == 0) {
				EmployeeProfile emp = employeeRepo.findAll().stream().findFirst().orElse(null);
				if (emp != null) {
					QuarterlyReview qr = new QuarterlyReview();
					qr.setEmployee(emp);
					qr.setReviewDate(LocalDate.now().minusDays(30));
					qr.setRating(4);
					qr.setFeedback("Strong performance in Q1");
					qr.setQuarter("Q1 " + LocalDate.now().getYear());
					perfRepo.save(qr);

					AnnualReview ar = new AnnualReview();
					ar.setEmployee(emp);
					ar.setReviewDate(LocalDate.now().minusDays(200));
					ar.setRating(5);
					ar.setFeedback("Excellent year");
					ar.setYear(LocalDate.now().getYear() - 1);
					perfRepo.save(ar);
				}
			}
			if (kpiRepo.count() == 0) {
				EmployeeProfile emp = employeeRepo.findAll().stream().findFirst().orElse(null);
				if (emp != null) {
					Kpi k = new Kpi();
					k.setEmployee(emp);
					k.setName("Sales Targets");
					k.setDescription("Quarterly sales");
					k.setTargetValue(100.0);
					k.setCurrentValue(75.0);
					kpiRepo.save(k);
				}
			}
		};
	}

	@Bean
	CommandLineRunner seedShifts(ShiftRepository shiftRepo, EmployeeProfileRepository employeeRepo) {
		return args -> {
			if (shiftRepo.count() == 0) {
				List<EmployeeProfile> employees = employeeRepo.findAll();
				if (!employees.isEmpty()) {
					// Create shifts for different employees
					EmployeeProfile sadeni = employees.stream()
						.filter(emp -> emp.getFirstName().equals("Sadeni"))
						.findFirst().orElse(employees.get(1)); // Fallback to second employee
					
					EmployeeProfile nethmi = employees.stream()
						.filter(emp -> emp.getFirstName().equals("Nethmi"))
						.findFirst().orElse(employees.get(2)); // Fallback to third employee
					
					EmployeeProfile tharushi = employees.stream()
						.filter(emp -> emp.getFirstName().equals("Tharushi"))
						.findFirst().orElse(employees.get(3)); // Fallback to fourth employee

					// Day shift for Sadeni
					DayShift ds1 = new DayShift();
					ds1.setEmployee(sadeni);
					ds1.setShiftDate(LocalDate.now().plusDays(1));
					ds1.setStartTime(LocalTime.of(9, 0));
					ds1.setEndTime(LocalTime.of(17, 0));
					shiftRepo.save(ds1);

					// Night shift for Nethmi
					NightShift ns1 = new NightShift();
					ns1.setEmployee(nethmi);
					ns1.setShiftDate(LocalDate.now().plusDays(2));
					ns1.setStartTime(LocalTime.of(22, 0));
					ns1.setEndTime(LocalTime.of(6, 0));
					shiftRepo.save(ns1);

					// Day shift for Tharushi
					DayShift ds2 = new DayShift();
					ds2.setEmployee(tharushi);
					ds2.setShiftDate(LocalDate.now().plusDays(3));
					ds2.setStartTime(LocalTime.of(8, 0));
					ds2.setEndTime(LocalTime.of(16, 0));
					shiftRepo.save(ds2);

					// Night shift for Sadeni
					NightShift ns2 = new NightShift();
					ns2.setEmployee(sadeni);
					ns2.setShiftDate(LocalDate.now().plusDays(4));
					ns2.setStartTime(LocalTime.of(23, 0));
					ns2.setEndTime(LocalTime.of(7, 0));
					shiftRepo.save(ns2);
				}
			}
		};
	}

    // Removed sample notification seeding to avoid creating demo data in production

	@Bean
	CommandLineRunner seedLeaveBalances(LeaveBalanceRepository leaveBalanceRepo, EmployeeProfileRepository employeeRepo) {
		return args -> {
			try {
				if (leaveBalanceRepo.count() == 0) {
					var employees = employeeRepo.findAll();
					int currentYear = java.time.LocalDate.now().getYear();
					
					for (var employee : employees) {
						// Create standard leave balances for each employee
						LeaveBalance vacation = new LeaveBalance(employee, LeaveBalance.LeaveType.VACATION, 20, currentYear);
						LeaveBalance sick = new LeaveBalance(employee, LeaveBalance.LeaveType.SICK, 10, currentYear);
						LeaveBalance personal = new LeaveBalance(employee, LeaveBalance.LeaveType.PERSONAL, 5, currentYear);
						LeaveBalance emergency = new LeaveBalance(employee, LeaveBalance.LeaveType.EMERGENCY, 3, currentYear);
						
						leaveBalanceRepo.save(vacation);
						leaveBalanceRepo.save(sick);
						leaveBalanceRepo.save(personal);
						leaveBalanceRepo.save(emergency);
					}
				}
			} catch (Exception e) {
				log.warn("Could not seed leave balances: {}", e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner seedSamplePermissions(PermissionService permissionService, UserAccountRepository userRepo) {
		return args -> {
			try {
				// Grant some sample permissions to demonstrate the system
				UserAccount tharushi = userRepo.findByUsername("tharushi").orElse(null);
				UserAccount hiruni = userRepo.findByUsername("hiruni").orElse(null);
				UserAccount nethmi = userRepo.findByUsername("nethmi").orElse(null);
				
			if (tharushi != null) {
				// Grant EMPLOYEE_VIEW permission to tharushi
				permissionService.grantPermission(tharushi, "EMPLOYEE_VIEW", "system");
				permissionService.grantPermission(tharushi, "ATTENDANCE_VIEW", "system");
				permissionService.grantPermission(tharushi, "SHIFT_VIEW", "system");
				log.info("Granted sample permissions to tharushi");
			}
				
			if (hiruni != null) {
				// Grant ATTENDANCE_VIEW permission to hiruni
				permissionService.grantPermission(hiruni, "ATTENDANCE_VIEW", "system");
				permissionService.grantPermission(hiruni, "LEAVE_VIEW", "system");
				log.info("Granted sample permissions to hiruni");
			}
				
			if (nethmi != null) {
				// Grant REPORTS_VIEW permission to nethmi (manager)
				permissionService.grantPermission(nethmi, "REPORTS_VIEW", "system");
				permissionService.grantPermission(nethmi, "EMPLOYEE_VIEW", "system");
				permissionService.grantPermission(nethmi, "PERFORMANCE_VIEW", "system");
				log.info("Granted sample permissions to nethmi");
			}
			} catch (Exception e) {
				log.warn("Could not seed sample permissions: {}", e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner logStartup(UserAccountRepository userRepo) {
		return args -> {
			log.info("====================================================");
			log.info("Default in-memory users (Spring Security):");
			log.info("  admin / admin123  [ROLE_ADMIN]");
			log.info("  hr / hr123        [ROLE_HR]");
			log.info("  manager / manager123 [ROLE_MANAGER]");
			log.info("  employee / emp123 [ROLE_EMPLOYEE]");
			log.info("");
			log.info("Default database users (H2) created on first run:");
			log.info("  db-admin / admin123   [ROLE_ADMIN]");
			log.info("  db-hr / hr123         [ROLE_HR]");
			log.info("  db-manager / manager123 [ROLE_MANAGER]");
			log.info("  db-employee / emp123  [ROLE_EMPLOYEE]");
			log.info("");
			log.info("Additional sample users:");
			log.info("  dinaya / dinaya123    [ROLE_ADMIN]");
			log.info("  sadeni / sadeni123    [ROLE_HR]");
			log.info("  nethmi / nethmi123    [ROLE_MANAGER]");
			log.info("  tharushi / tharushi123 [ROLE_EMPLOYEE]");
			log.info("  hiruni / hiruni123    [ROLE_EMPLOYEE]");
			log.info("");
			log.info("MS SQL Server Configuration:");
			log.info("  - Server: localhost:1433  |  Database: emsdb  |  user: sa  |  password: YourPassword123!");
			log.info("  - Connection: jdbc:sqlserver://localhost:1433;databaseName=emsdb;encrypt=true;trustServerCertificate=true");
			log.info("Users in DB now: {}", userRepo.count());
			log.info("EMS application started successfully ✅");
			log.info("====================================================");
		};
	}
}


