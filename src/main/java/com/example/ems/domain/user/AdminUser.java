package com.example.ems.domain.user;

import jakarta.persistence.Column;
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
@DiscriminatorValue("ADMIN")
public class AdminUser extends UserAccount {

    @Column(name = "last_admin_action")
    private LocalDateTime lastAdminAction;
    @Column(name = "admin_actions_count")
    private int adminActionsCount = 0;
    @Column(name = "super_admin")
    private boolean superAdmin = false;

    public AdminUser(String username, String password, boolean enabled) {
        super();
        this.setUsername(username);
        this.setPassword(password);
        this.setEnabled(enabled);
    }

    @Override
    public boolean canAccess(String resource) {
        // Admin users have access to all resources
        return true;
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }

    @Override
    public boolean canViewReports() {
        return true;
    }

    @Override
    public boolean canManageSystem() {
        return true;
    }

    public void recordAdminAction() {
        this.adminActionsCount++;
        this.lastAdminAction = LocalDateTime.now();
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    @Override
    public Set<String> getPermissions() {
        return Set.of(
            "USER_MANAGEMENT",
            "ROLE_MANAGEMENT", 
            "SYSTEM_CONFIGURATION",
            "REPORT_ACCESS",
            "DATA_EXPORT",
            "SECURITY_AUDIT",
            "BACKUP_RESTORE"
        );
    }
}
