package com.example.ems.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;

	private String password;

	private boolean enabled = true;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	// Security fields
	@Column(name = "last_login")
	private LocalDateTime lastLogin;
	@Column(name = "account_created")
	private LocalDateTime accountCreated = LocalDateTime.now();
	@Column(name = "failed_login_attempts")
	private int failedLoginAttempts = 0;
	@Column(name = "account_locked_until")
	private LocalDateTime accountLockedUntil;
	@Column(name = "account_non_expired")
	private boolean accountNonExpired = true;
	@Column(name = "credentials_non_expired")
	private boolean credentialsNonExpired = true;
	@Column(name = "account_non_locked")
	private boolean accountNonLocked = true;

	// Abstract methods for polymorphism
	public abstract boolean canAccess(String resource);
	public abstract boolean canManageUsers();
	public abstract boolean canViewReports();
	public abstract boolean canManageSystem();
	public abstract String getUserType();
	public abstract Set<String> getPermissions();
	
	// Dynamic permission checking methods (to be overridden by subclasses if needed)
	public boolean hasPermission(String permissionName) {
		// This will be enhanced by the PermissionService
		// For now, return false - this should be overridden by subclasses or enhanced by services
		return false;
	}
	
	public boolean hasAnyPermission(String... permissionNames) {
		for (String permissionName : permissionNames) {
			if (hasPermission(permissionName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAllPermissions(String... permissionNames) {
		for (String permissionName : permissionNames) {
			if (!hasPermission(permissionName)) {
				return false;
			}
		}
		return true;
	}

	// Common security methods
	public boolean isAccountLocked() {
		return accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil);
	}

	public void incrementFailedLoginAttempts() {
		this.failedLoginAttempts++;
		if (this.failedLoginAttempts >= 5) {
			this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
		}
	}

	public void resetFailedLoginAttempts() {
		this.failedLoginAttempts = 0;
		this.accountLockedUntil = null;
	}

	public void recordLogin() {
		this.lastLogin = LocalDateTime.now();
		resetFailedLoginAttempts();
	}

	public boolean hasRole(String roleName) {
		return roles.stream().anyMatch(role -> role.getName().equals(roleName));
	}

	public boolean hasAnyRole(String... roleNames) {
		for (String roleName : roleNames) {
			if (hasRole(roleName)) {
				return true;
			}
		}
		return false;
	}

	public String getDisplayName() {
		return username;
	}

	public boolean isActive() {
		return enabled && !isAccountLocked() && accountNonExpired && credentialsNonExpired && accountNonLocked;
	}
}


