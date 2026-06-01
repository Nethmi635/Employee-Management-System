package com.example.ems.domain.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("EMPLOYEE")
public class EmployeeUser extends UserAccount {

    private LocalDateTime lastLogin;
    private int loginCount = 0;
    private String employeeId;
    private String department;
    private String position;
    private boolean canRequestLeave = true;
    private boolean canViewOwnData = true;

    public EmployeeUser(String username, String password, boolean enabled) {
        super();
        this.setUsername(username);
        this.setPassword(password);
        this.setEnabled(enabled);
    }

    @Override
    public boolean canAccess(String resource) {
        return switch (resource) {
            case "/admin/**", "/hr/**", "/manager/**", "/approvals/**" -> false;
            case "/profile/**", "/attendance/**", "/leave/**", "/performance/**" -> true;
            default -> false;
        };
    }

    @Override
    public boolean canManageUsers() {
        return false; // Employees cannot manage users
    }

    @Override
    public boolean canViewReports() {
        return false; // Employees cannot view reports
    }

    @Override
    public boolean canManageSystem() {
        return false; // Employees cannot manage system
    }

    public void recordLogin() {
        this.loginCount++;
        this.lastLogin = LocalDateTime.now();
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String getUserType() {
        return "EMPLOYEE";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of(
            "PROFILE_VIEW",
            "PROFILE_EDIT",
            "ATTENDANCE_VIEW",
            "LEAVE_REQUEST",
            "PERFORMANCE_VIEW",
            "SHIFT_VIEW"
        );
    }
}
