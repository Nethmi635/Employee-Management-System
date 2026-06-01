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
@DiscriminatorValue("MANAGER")
public class ManagerUser extends UserAccount {

    private LocalDateTime lastTeamReview;
    private int teamSize = 0;
    private String department;
    private boolean canApproveLeave = true;
    private boolean canApproveOvertime = true;

    public ManagerUser(String username, String password, boolean enabled) {
        super();
        this.setUsername(username);
        this.setPassword(password);
        this.setEnabled(enabled);
    }

    @Override
    public boolean canAccess(String resource) {
        return switch (resource) {
            case "/admin/**" -> false; // Managers cannot access admin areas
            case "/hr/**" -> false; // Managers cannot access HR areas
            case "/manager/**", "/attendance/**", "/leave/**", "/approvals/**", 
                 "/performance/**", "/shifts/**", "/reports/**" -> true;
            default -> true;
        };
    }

    @Override
    public boolean canManageUsers() {
        return false; // Managers cannot manage users
    }

    @Override
    public boolean canViewReports() {
        return true;
    }

    @Override
    public boolean canManageSystem() {
        return false; // Managers cannot manage system
    }

    public void recordTeamReview() {
        this.lastTeamReview = LocalDateTime.now();
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getUserType() {
        return "MANAGER";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of(
            "TEAM_MANAGEMENT",
            "LEAVE_APPROVAL",
            "OVERTIME_APPROVAL",
            "PERFORMANCE_REVIEW",
            "SHIFT_MANAGEMENT",
            "REPORT_ACCESS",
            "ATTENDANCE_OVERSIGHT"
        );
    }
}
