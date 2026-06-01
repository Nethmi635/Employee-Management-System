package com.example.ems.service;

import com.example.ems.domain.employee.ContractEmployee;
import com.example.ems.domain.employee.EmployeeDocument;
import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.employee.FullTimeEmployee;
import com.example.ems.repository.ContractEmployeeRepository;
import com.example.ems.repository.EmployeeDocumentRepository;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.repository.FullTimeEmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

	private final EmployeeProfileRepository employeeProfileRepository;
    private final FullTimeEmployeeRepository fullTimeEmployeeRepository;
    private final ContractEmployeeRepository contractEmployeeRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public EmployeeService(EmployeeProfileRepository employeeProfileRepository,
                         FullTimeEmployeeRepository fullTimeEmployeeRepository,
                         ContractEmployeeRepository contractEmployeeRepository,
                         EmployeeDocumentRepository employeeDocumentRepository) {
        this.employeeProfileRepository = employeeProfileRepository;
        this.fullTimeEmployeeRepository = fullTimeEmployeeRepository;
        this.contractEmployeeRepository = contractEmployeeRepository;
        this.employeeDocumentRepository = employeeDocumentRepository;
    }

    public List<EmployeeProfile> listAll(String q) {
        if (q == null || q.isBlank()) {
            return employeeProfileRepository.findAll();
        }
        return employeeProfileRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, q);
    }

    public List<EmployeeProfile> listAll(String q, Boolean active) {
        List<EmployeeProfile> base = listAll(q);
        if (active == null) return base;
        return base.stream().filter(e -> active.equals(e.getActive())).toList();
    }

    public List<EmployeeProfile> listAll(String q,
                                         Boolean active,
                                         String department,
                                         String jobTitle,
                                         String employmentType) {
        List<EmployeeProfile> result = listAll(q, active);

        if (department != null && !department.isBlank()) {
            String dep = department.trim();
            result = result.stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(dep))
                .toList();
        }

        if (jobTitle != null && !jobTitle.isBlank()) {
            String title = jobTitle.trim();
            result = result.stream()
                .filter(e -> e.getJobTitle() != null && e.getJobTitle().equalsIgnoreCase(title))
                .toList();
        }

        if (employmentType != null && !employmentType.isBlank()) {
            String type = employmentType.trim().toUpperCase();
            result = result.stream()
                .filter(e -> {
                    if ("FULL_TIME".equals(type) || "FULLTIME".equals(type)) {
                        return e instanceof FullTimeEmployee;
                    }
                    if ("CONTRACT".equals(type) || "CONTRACTOR".equals(type)) {
                        return e instanceof ContractEmployee;
                    }
                    return true;
                })
                .toList();
        }

        return result;
    }

	public Optional<EmployeeProfile> findById(Long id) {
		return employeeProfileRepository.findById(id);
	}

	public EmployeeProfile save(EmployeeProfile employeeProfile) {
		return employeeProfileRepository.save(employeeProfile);
	}

	public void delete(Long id) {
		employeeProfileRepository.deleteById(id);
	}

	public FullTimeEmployee saveFullTime(FullTimeEmployee e) {
		return fullTimeEmployeeRepository.save(e);
	}

	public ContractEmployee saveContract(ContractEmployee e) {
		return contractEmployeeRepository.save(e);
	}

    public List<EmployeeDocument> listDocuments(EmployeeProfile employee) {
        return employeeDocumentRepository.findByEmployee(employee);
    }

    public EmployeeDocument storeDocument(EmployeeProfile employee, MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String stored = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir).resolve(stored).normalize();
        Files.copy(file.getInputStream(), target);

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStoredFilename(stored);
        doc.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        return employeeDocumentRepository.save(doc);
    }

    public void deleteDocument(EmployeeDocument doc) throws IOException {
        Path path = Paths.get(uploadDir).resolve(doc.getStoredFilename()).normalize();
        Files.deleteIfExists(path);
        employeeDocumentRepository.delete(doc);
    }

    public String storeProfilePhoto(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String stored = "photo-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir).resolve(stored).normalize();
        Files.copy(file.getInputStream(), target);
        return stored;
    }
}


