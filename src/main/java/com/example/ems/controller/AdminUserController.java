package com.example.ems.controller;

import com.example.ems.domain.user.*;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.NotificationService;
import com.example.ems.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final UserAccountRepository userRepo;
	private final RoleRepository roleRepo;
	private final PasswordEncoder passwordEncoder;
	private final NotificationService notificationService;
	private final PermissionService permissionService;

	public AdminUserController(UserAccountRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder, NotificationService notificationService, PermissionService permissionService) {
		this.userRepo = userRepo;
		this.roleRepo = roleRepo;
		this.passwordEncoder = passwordEncoder;
		this.notificationService = notificationService;
		this.permissionService = permissionService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("users", userRepo.findAll());
		model.addAttribute("roles", roleRepo.findAll());
		return "admin/users/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("userTypes", List.of("ADMIN", "MANAGER", "EMPLOYEE"));
		model.addAttribute("roles", roleRepo.findAll());
		return "admin/users/new";
	}

	@PostMapping
	public String create(@RequestParam String username,
					   @RequestParam String password,
					   @RequestParam String userType,
					   @RequestParam(required = false) Set<Long> roleIds) {
		UserAccount u = createUserByType(userType, username, password, true);
		if (roleIds != null && !roleIds.isEmpty()) {
			Set<Role> roles = new HashSet<>(roleRepo.findAllById(roleIds));
			u.setRoles(roles);
		}
		userRepo.save(u);
		
		// Send system alert to all admins about new user account
		notifyAdminsAboutNewUserAccount(u);
		
		return "redirect:/admin/users";
	}

	private UserAccount createUserByType(String userType, String username, String password, boolean enabled) {
		return switch (userType.toUpperCase()) {
			case "ADMIN" -> {
				AdminUser admin = new AdminUser(username, passwordEncoder.encode(password), enabled);
				yield userRepo.save(admin);
			}
			case "MANAGER" -> {
				ManagerUser manager = new ManagerUser(username, passwordEncoder.encode(password), enabled);
				yield userRepo.save(manager);
			}
			case "EMPLOYEE" -> {
				EmployeeUser employee = new EmployeeUser(username, passwordEncoder.encode(password), enabled);
				yield userRepo.save(employee);
			}
			default -> throw new IllegalArgumentException("Invalid user type: " + userType);
		};
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		UserAccount u = userRepo.findById(id).orElseThrow();
		model.addAttribute("user", u);
		model.addAttribute("roles", roleRepo.findAll());
		
		// Add permission data
		List<Permission> allPermissions = permissionService.getAllPermissions();
		List<String> userPermissions = permissionService.getUserPermissions(u);
		Map<String, List<Permission>> permissionsByCategory = allPermissions.stream()
			.collect(java.util.stream.Collectors.groupingBy(Permission::getCategory));
		
		model.addAttribute("allPermissions", allPermissions);
		model.addAttribute("userPermissions", userPermissions);
		model.addAttribute("permissionsByCategory", permissionsByCategory);
		model.addAttribute("permissionCategories", permissionService.getPermissionCategories());
		
		return "admin/users/edit";
	}

	@PostMapping("/{id}/edit")
	public String update(@PathVariable Long id,
					  @RequestParam String username,
					  @RequestParam(required = false) String password,
					  @RequestParam(required = false, defaultValue = "false") boolean enabled,
					  @RequestParam(required = false) Set<Long> roleIds,
					  @RequestParam(required = false) Set<String> permissionNames,
					  Authentication authentication,
					  RedirectAttributes redirectAttributes) {
		try {
			UserAccount u = userRepo.findById(id).orElseThrow();
			String originalUsername = u.getUsername();
			
			// Update basic user information
			u.setUsername(username);
			u.setEnabled(enabled);
			if (password != null && !password.isBlank()) {
				u.setPassword(passwordEncoder.encode(password));
			}
			
			// Update roles
			Set<Role> roles = roleIds == null ? new HashSet<>() : new HashSet<>(roleRepo.findAllById(roleIds));
			u.setRoles(roles);
			userRepo.save(u);
			
			// Handle permission updates
			String grantedBy = authentication.getName();
			List<String> currentPermissions = permissionService.getUserPermissions(u);
			Set<String> newPermissions = permissionNames != null ? permissionNames : new HashSet<>();
			
			int permissionsGranted = 0;
			int permissionsRevoked = 0;
			
			// Revoke permissions that are no longer selected
			for (String currentPermission : currentPermissions) {
				if (!newPermissions.contains(currentPermission)) {
					try {
						permissionService.revokePermission(u, currentPermission);
						permissionsRevoked++;
					} catch (Exception e) {
						System.err.println("Error revoking permission " + currentPermission + ": " + e.getMessage());
					}
				}
			}
			
			// Grant new permissions
			for (String permissionName : newPermissions) {
				if (!currentPermissions.contains(permissionName)) {
					try {
						permissionService.grantPermission(u, permissionName, grantedBy);
						permissionsGranted++;
					} catch (Exception e) {
						System.err.println("Error granting permission " + permissionName + ": " + e.getMessage());
					}
				}
			}
			
			// Add success message
			StringBuilder message = new StringBuilder("User '").append(username).append("' updated successfully!");
			if (permissionsGranted > 0 || permissionsRevoked > 0) {
				message.append(" Permissions: ").append(permissionsGranted).append(" granted, ").append(permissionsRevoked).append(" revoked.");
			}
			redirectAttributes.addFlashAttribute("successMessage", message.toString());
			
			// Log the changes
			System.out.println("User updated by " + grantedBy + ": " + originalUsername + " -> " + username + 
							   " (Permissions: +" + permissionsGranted + ", -" + permissionsRevoked + ")");
			
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
			System.err.println("Error updating user " + id + ": " + e.getMessage());
		}
		
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/permissions")
	public String permissionsForm(@PathVariable Long id, Model model) {
		UserAccount u = userRepo.findById(id).orElseThrow();
		model.addAttribute("user", u);
		
		// Add permission data
		List<Permission> allPermissions = permissionService.getAllPermissions();
		List<String> userPermissions = permissionService.getUserPermissions(u);
		Map<String, List<Permission>> permissionsByCategory = allPermissions.stream()
			.collect(java.util.stream.Collectors.groupingBy(Permission::getCategory));
		
		model.addAttribute("allPermissions", allPermissions);
		model.addAttribute("userPermissions", userPermissions);
		model.addAttribute("permissionsByCategory", permissionsByCategory);
		model.addAttribute("permissionCategories", permissionService.getPermissionCategories());
		
		return "admin/users/permissions";
	}

	@PostMapping("/{id}/permissions")
	public String updatePermissions(@PathVariable Long id,
								   @RequestParam(required = false) Set<String> permissionNames,
								   Authentication authentication,
								   RedirectAttributes redirectAttributes) {
		try {
			UserAccount u = userRepo.findById(id).orElseThrow();
			String grantedBy = authentication.getName();
			List<String> currentPermissions = permissionService.getUserPermissions(u);
			Set<String> newPermissions = permissionNames != null ? permissionNames : new HashSet<>();
			
			int permissionsGranted = 0;
			int permissionsRevoked = 0;
			
			// Revoke permissions that are no longer selected
			for (String currentPermission : currentPermissions) {
				if (!newPermissions.contains(currentPermission)) {
					try {
						permissionService.revokePermission(u, currentPermission);
						permissionsRevoked++;
					} catch (Exception e) {
						System.err.println("Error revoking permission " + currentPermission + ": " + e.getMessage());
					}
				}
			}
			
			// Grant new permissions
			for (String permissionName : newPermissions) {
				if (!currentPermissions.contains(permissionName)) {
					try {
						permissionService.grantPermission(u, permissionName, grantedBy);
						permissionsGranted++;
					} catch (Exception e) {
						System.err.println("Error granting permission " + permissionName + ": " + e.getMessage());
					}
				}
			}
			
			// Add success message
			StringBuilder message = new StringBuilder("Permissions updated for '").append(u.getUsername()).append("'!");
			message.append(" Granted: ").append(permissionsGranted).append(", Revoked: ").append(permissionsRevoked);
			redirectAttributes.addFlashAttribute("successMessage", message.toString());
			
			// Log the changes
			System.out.println("Permissions updated by " + grantedBy + " for user " + u.getUsername() + 
							   " (Permissions: +" + permissionsGranted + ", -" + permissionsRevoked + ")");
			
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating permissions: " + e.getMessage());
			System.err.println("Error updating permissions for user " + id + ": " + e.getMessage());
		}
		
		return "redirect:/admin/users";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			UserAccount u = userRepo.findById(id).orElseThrow();
			String username = u.getUsername();
			userRepo.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "User '" + username + "' deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
		}
		return "redirect:/admin/users";
	}

	@PostMapping("/roles")
	public String createRole(@RequestParam String name) {
		roleRepo.findByName(name).orElseGet(() -> {
			Role r = new Role();
			r.setName(name);
			return roleRepo.save(r);
		});
		return "redirect:/admin/users";
	}

	@PostMapping("/roles/{id}/delete")
	public String deleteRole(@PathVariable Long id) {
		roleRepo.deleteById(id);
		return "redirect:/admin/users";
	}
	
	// Permission Management Endpoints
	@GetMapping("/permissions")
	public String permissions(Model model) {
		List<Permission> permissions = permissionService.getAllPermissions();
		List<UserAccount> users = userRepo.findAll();
		Map<String, Map<String, Boolean>> permissionMatrix = permissionService.getPermissionMatrix(users, permissions);
		
		model.addAttribute("permissions", permissions);
		model.addAttribute("users", users);
		model.addAttribute("permissionMatrix", permissionMatrix);
		model.addAttribute("permissionCategories", permissionService.getPermissionCategories());
		
		return "admin/users/permissions";
	}

	@PostMapping("/permissions/grant")
	public String grantPermission(@RequestParam Long userId, 
								@RequestParam String permissionName,
								Authentication authentication) {
		UserAccount user = userRepo.findById(userId).orElseThrow();
		permissionService.grantPermission(user, permissionName, authentication.getName());
		return "redirect:/admin/users/permissions";
	}

	@PostMapping("/permissions/revoke")
	public String revokePermission(@RequestParam Long userId, 
								 @RequestParam String permissionName) {
		UserAccount user = userRepo.findById(userId).orElseThrow();
		permissionService.revokePermission(user, permissionName);
		return "redirect:/admin/users/permissions";
	}

	@PostMapping("/permissions/bulk")
	public String bulkPermissionUpdate(@RequestParam Long userId,
									 @RequestParam(required = false) Set<String> permissionNames,
									 Authentication authentication) {
		UserAccount user = userRepo.findById(userId).orElseThrow();
		
		if (permissionNames == null) {
			permissionService.revokeAllPermissions(user);
		} else {
			permissionService.grantPermissions(user, List.copyOf(permissionNames), authentication.getName());
		}
		
		return "redirect:/admin/users/permissions";
	}

	// Helper method to notify admins about new user account creation
	private void notifyAdminsAboutNewUserAccount(UserAccount newUser) {
		try {
			// Get all admin users except the one who created the account
			List<UserAccount> adminUsers = userRepo.findAll().stream()
				.filter(user -> user.hasRole("ADMIN"))
				.filter(user -> !user.getId().equals(newUser.getId()))
				.toList();
			
			// Send notification to each admin
			for (UserAccount admin : adminUsers) {
				notificationService.createSystemAlert(
					admin,
					"New User Account Created",
					String.format("A new %s user account '%s' has been created in the system.", 
						newUser.getUserType(), newUser.getUsername())
				);
			}
		} catch (Exception e) {
			// Log error but don't break the user creation process
			System.err.println("Failed to send notification about new user account: " + e.getMessage());
		}
	}
}


