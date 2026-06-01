package com.example.ems.service;

import com.example.ems.domain.user.*;
import com.example.ems.repository.PasswordResetTokenRepository;
import com.example.ems.repository.RoleRepository;
import com.example.ems.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementService {

    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserAccountRepository userRepo, RoleRepository roleRepo,
                               PasswordResetTokenRepository resetTokenRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.resetTokenRepo = resetTokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // User Management
    public List<UserAccount> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<UserAccount> getUserById(Long id) {
        return userRepo.findById(id);
    }

    public Optional<UserAccount> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public UserAccount createUser(String username, String password, String userType, Set<Long> roleIds) {
        UserAccount user = createUserByType(userType, username, password, true);
        
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = roleRepo.findAllById(roleIds).stream().collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        return userRepo.save(user);
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

    public UserAccount updateUser(Long id, String username, String password, boolean enabled, Set<Long> roleIds) {
        UserAccount user = userRepo.findById(id).orElseThrow();
        user.setUsername(username);
        user.setEnabled(enabled);
        
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        if (roleIds != null) {
            Set<Role> roles = roleIds.isEmpty() ? 
                new java.util.HashSet<>() : 
                roleRepo.findAllById(roleIds).stream().collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    // Password Management
    public PasswordResetToken createPasswordResetToken(String username) {
        UserAccount user = userRepo.findByUsername(username).orElseThrow();
        
        // Invalidate existing tokens for this user
        resetTokenRepo.findByUser(user).forEach(token -> {
            token.markAsUsed();
            resetTokenRepo.save(token);
        });
        
        PasswordResetToken token = new PasswordResetToken(user);
        return resetTokenRepo.save(token);
    }

    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepo.findByToken(token).orElseThrow();
        
        if (!resetToken.isValid()) {
            return false;
        }
        
        UserAccount user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetFailedLoginAttempts();
        userRepo.save(user);
        
        resetToken.markAsUsed();
        resetTokenRepo.save(resetToken);
        
        return true;
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserAccount user = userRepo.findById(userId).orElseThrow();
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    // Security Management
    public void lockUser(Long userId, int minutes) {
        UserAccount user = userRepo.findById(userId).orElseThrow();
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(minutes));
        userRepo.save(user);
    }

    public void unlockUser(Long userId) {
        UserAccount user = userRepo.findById(userId).orElseThrow();
        user.resetFailedLoginAttempts();
        userRepo.save(user);
    }

    public void enableUser(Long userId) {
        UserAccount user = userRepo.findById(userId).orElseThrow();
        user.setEnabled(true);
        userRepo.save(user);
    }

    public void disableUser(Long userId) {
        UserAccount user = userRepo.findById(userId).orElseThrow();
        user.setEnabled(false);
        userRepo.save(user);
    }

    // Analytics and Statistics
    public Map<String, Object> getUserStatistics() {
        List<UserAccount> allUsers = userRepo.findAll();
        
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(UserAccount::isActive).count();
        long lockedUsers = allUsers.stream().filter(UserAccount::isAccountLocked).count();
        long disabledUsers = allUsers.stream().filter(u -> !u.isEnabled()).count();
        
        Map<String, Long> usersByType = allUsers.stream()
            .collect(Collectors.groupingBy(UserAccount::getUserType, Collectors.counting()));
        
        Map<String, Long> usersByRole = allUsers.stream()
            .flatMap(user -> user.getRoles().stream())
            .collect(Collectors.groupingBy(Role::getName, Collectors.counting()));
        
        return Map.of(
            "totalUsers", totalUsers,
            "activeUsers", activeUsers,
            "lockedUsers", lockedUsers,
            "disabledUsers", disabledUsers,
            "usersByType", usersByType,
            "usersByRole", usersByRole
        );
    }

    public List<UserAccount> getUsersByType(String userType) {
        return userRepo.findAll().stream()
            .filter(user -> user.getUserType().equals(userType.toUpperCase()))
            .collect(Collectors.toList());
    }

    public List<UserAccount> getInactiveUsers() {
        return userRepo.findAll().stream()
            .filter(user -> !user.isActive())
            .collect(Collectors.toList());
    }

    // Role Management
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    public Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepo.save(role);
    }

    public void deleteRole(Long id) {
        roleRepo.deleteById(id);
    }

    // Access Control
    public boolean hasAccess(UserAccount user, String resource) {
        return user.canAccess(resource);
    }

    public boolean canManageUsers(UserAccount user) {
        return user.canManageUsers();
    }

    public boolean canViewReports(UserAccount user) {
        return user.canViewReports();
    }

    public boolean canManageSystem(UserAccount user) {
        return user.canManageSystem();
    }
}
