package com.example.ems.service;

import com.example.ems.domain.user.*;
import com.example.ems.repository.PermissionRepository;
import com.example.ems.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    public PermissionService(PermissionRepository permissionRepository, UserPermissionRepository userPermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    // Permission Management
    public Permission createPermission(String name, String category, String description, boolean systemPermission) {
        if (permissionRepository.existsByName(name)) {
            throw new IllegalArgumentException("Permission with name '" + name + "' already exists");
        }
        
        Permission permission = new Permission(name, category, description, systemPermission);
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<Permission> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category);
    }

    public List<String> getPermissionCategories() {
        return permissionRepository.findDistinctCategories();
    }

    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        
        if (permission.isSystemPermission()) {
            throw new IllegalArgumentException("Cannot delete system permission: " + permission.getName());
        }
        
        // Check if any users have this permission
        long userCount = userPermissionRepository.countActiveUsersWithPermission(permission);
        if (userCount > 0) {
            throw new IllegalArgumentException("Cannot delete permission. " + userCount + " users currently have this permission.");
        }
        
        permissionRepository.delete(permission);
    }

    // User Permission Management
    public void grantPermission(UserAccount user, String permissionName, String grantedBy) {
        Permission permission = permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
        
        // Check if user already has this permission
        Optional<UserPermission> existingPermission = userPermissionRepository.findByUserAndPermission(user, permission);
        
        if (existingPermission.isPresent()) {
            UserPermission up = existingPermission.get();
            if (up.isEffective()) {
                throw new IllegalArgumentException("User already has this permission");
            } else {
                // Reactivate the permission
                up.setActive(true);
                up.setGrantedBy(grantedBy);
                up.setGrantedAt(LocalDateTime.now());
                up.setExpiresAt(null);
                userPermissionRepository.save(up);
            }
        } else {
            // Create new permission
            UserPermission userPermission = new UserPermission(user, permission, grantedBy);
            userPermissionRepository.save(userPermission);
        }
    }

    public void revokePermission(UserAccount user, String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
        
        Optional<UserPermission> userPermission = userPermissionRepository.findByUserAndPermission(user, permission);
        if (userPermission.isPresent()) {
            userPermission.get().setActive(false);
            userPermissionRepository.save(userPermission.get());
        }
    }

    public void revokeAllPermissions(UserAccount user) {
        List<UserPermission> userPermissions = userPermissionRepository.findByUser(user);
        for (UserPermission up : userPermissions) {
            up.setActive(false);
        }
        userPermissionRepository.saveAll(userPermissions);
    }

    public List<String> getUserPermissions(UserAccount user) {
        return userPermissionRepository.findEffectivePermissionNamesByUser(user, LocalDateTime.now());
    }

    public List<UserPermission> getUserPermissionDetails(UserAccount user) {
        return userPermissionRepository.findEffectivePermissionsByUser(user, LocalDateTime.now());
    }

    public boolean hasPermission(UserAccount user, String permissionName) {
        return userPermissionRepository.findEffectivePermissionNamesByUser(user, LocalDateTime.now())
            .contains(permissionName);
    }

    public boolean hasAnyPermission(UserAccount user, String... permissionNames) {
        List<String> userPermissions = getUserPermissions(user);
        return Arrays.stream(permissionNames)
            .anyMatch(userPermissions::contains);
    }

    public boolean hasAllPermissions(UserAccount user, String... permissionNames) {
        List<String> userPermissions = getUserPermissions(user);
        return Arrays.stream(permissionNames)
            .allMatch(userPermissions::contains);
    }

    // Bulk operations
    public void grantPermissions(UserAccount user, List<String> permissionNames, String grantedBy) {
        for (String permissionName : permissionNames) {
            try {
                grantPermission(user, permissionName, grantedBy);
            } catch (IllegalArgumentException e) {
                // Log warning but continue with other permissions
                System.err.println("Warning: " + e.getMessage());
            }
        }
    }

    public void revokePermissions(UserAccount user, List<String> permissionNames) {
        for (String permissionName : permissionNames) {
            try {
                revokePermission(user, permissionName);
            } catch (IllegalArgumentException e) {
                // Log warning but continue with other permissions
                System.err.println("Warning: " + e.getMessage());
            }
        }
    }

    // Permission matrix for admin interface
    public Map<String, Map<String, Boolean>> getPermissionMatrix(List<UserAccount> users, List<Permission> permissions) {
        Map<String, Map<String, Boolean>> matrix = new HashMap<>();
        
        for (UserAccount user : users) {
            List<String> userPermissions = getUserPermissions(user);
            Map<String, Boolean> userPermissionMap = new HashMap<>();
            
            for (Permission permission : permissions) {
                userPermissionMap.put(permission.getName(), userPermissions.contains(permission.getName()));
            }
            
            matrix.put(user.getUsername(), userPermissionMap);
        }
        
        return matrix;
    }
}
