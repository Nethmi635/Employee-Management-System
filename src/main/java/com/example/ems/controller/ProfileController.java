package com.example.ems.controller;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserAccountRepository userRepo;
    private final EmployeeProfileRepository employeeRepo;
    private final EmployeeService employeeService;

    public ProfileController(UserAccountRepository userRepo, 
                           EmployeeProfileRepository employeeRepo,
                           EmployeeService employeeService) {
        this.userRepo = userRepo;
        this.employeeRepo = employeeRepo;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String viewProfile(Authentication auth, Model model) {
        String username = auth.getName();
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        // Try to find associated employee profile by email pattern
        Optional<EmployeeProfile> employeeProfile = employeeRepo.findByEmail(username + "@example.com");
        
        model.addAttribute("user", user);
        model.addAttribute("employee", employeeProfile.orElse(null));
        model.addAttribute("hasEmployeeProfile", employeeProfile.isPresent());
        
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(Authentication auth, Model model) {
        String username = auth.getName();
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        // Try to find associated employee profile by email pattern
        Optional<EmployeeProfile> employeeProfile = employeeRepo.findByEmail(username + "@example.com");
        
        model.addAttribute("user", user);
        model.addAttribute("employee", employeeProfile.orElse(null));
        model.addAttribute("hasEmployeeProfile", employeeProfile.isPresent());
        
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(Authentication auth,
                              @RequestParam(required = false) String firstName,
                              @RequestParam(required = false) String lastName,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String department,
                              @RequestParam(required = false) String jobTitle,
                              @RequestParam(required = false) MultipartFile photo) throws IOException {
        String username = auth.getName();
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        // Try to find associated employee profile by email pattern
        Optional<EmployeeProfile> employeeProfileOpt = employeeRepo.findByEmail(username + "@example.com");
        
        if (employeeProfileOpt.isPresent()) {
            EmployeeProfile employee = employeeProfileOpt.get();
            
            if (firstName != null && !firstName.trim().isEmpty()) {
                employee.setFirstName(firstName);
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                employee.setLastName(lastName);
            }
            if (email != null && !email.trim().isEmpty()) {
                employee.setEmail(email);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                employee.setPhone(phone);
            }
            if (department != null && !department.trim().isEmpty()) {
                employee.setDepartment(department);
            }
            if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                employee.setJobTitle(jobTitle);
            }
            if (photo != null && !photo.isEmpty()) {
                String filename = employeeService.storeProfilePhoto(photo);
                employee.setPhotoFilename(filename);
            }
            
            employeeService.save(employee);
        }
        
        return "redirect:/profile";
    }
}
