package com.example.ems.service;

import com.example.ems.domain.employee.ContractEmployee;
import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.employee.FullTimeEmployee;
import com.example.ems.repository.EmployeeProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeValidationService {

    private final EmployeeProfileRepository employeeProfileRepository;

    public EmployeeValidationService(EmployeeProfileRepository employeeProfileRepository) {
        this.employeeProfileRepository = employeeProfileRepository;
    }

    /**
     * Validates business rules for employee creation/update
     * @param employee The employee to validate
     * @return List of validation error messages
     */
    public List<String> validateEmployee(EmployeeProfile employee) {
        List<String> errors = new ArrayList<>();

        // Email uniqueness validation
        if (employee.getEmail() != null) {
            Optional<EmployeeProfile> existingEmployee = employeeProfileRepository.findByEmail(employee.getEmail());
            if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employee.getId())) {
                errors.add("Email address is already in use by another employee");
            }
        }

        // Salary validation for FullTimeEmployee
        if (employee instanceof FullTimeEmployee fullTimeEmployee) {
            if (fullTimeEmployee.getAnnualSalary() != null) {
                if (fullTimeEmployee.getAnnualSalary() < 20000) {
                    errors.add("Annual salary must be at least $20,000");
                }
                if (fullTimeEmployee.getAnnualSalary() > 1000000) {
                    errors.add("Annual salary cannot exceed $1,000,000");
                }
            }
        }

        // Hourly rate validation for ContractEmployee
        if (employee instanceof ContractEmployee contractEmployee) {
            if (contractEmployee.getHourlyRate() != null) {
                if (contractEmployee.getHourlyRate() < 10.0) {
                    errors.add("Hourly rate must be at least $10.00");
                }
                if (contractEmployee.getHourlyRate() > 500.0) {
                    errors.add("Hourly rate cannot exceed $500.00");
                }
            }
        }

        // Department validation against common departments
        if (employee.getDepartment() != null) {
            List<String> validDepartments = List.of(
                "Human Resources", "Information Technology", "Finance", "Marketing", 
                "Sales", "Operations", "Customer Service", "Legal", "Administration",
                "Engineering", "Research and Development", "Quality Assurance"
            );
            if (!validDepartments.contains(employee.getDepartment())) {
                errors.add("Please select a valid department from the list");
            }
        }

        return errors;
    }

    /**
     * Validates if the employee data is valid for creation
     * @param employee The employee to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidForCreation(EmployeeProfile employee) {
        return validateEmployee(employee).isEmpty();
    }

    /**
     * Gets validation error messages for display
     * @param employee The employee to validate
     * @return Formatted error messages
     */
    public String getValidationErrors(EmployeeProfile employee) {
        List<String> errors = validateEmployee(employee);
        return String.join(", ", errors);
    }
}
