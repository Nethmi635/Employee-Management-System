package com.example.ems.config;

import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.service.PermissionAwareUserService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionAwareUserService permissionAwareUserService;
    private final UserAccountRepository userAccountRepository;

    public CustomPermissionEvaluator(PermissionAwareUserService permissionAwareUserService, 
                                   UserAccountRepository userAccountRepository) {
        this.permissionAwareUserService = permissionAwareUserService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return false;
        }

        String permissionName = permission.toString();
        return permissionAwareUserService.hasPermission(user, permissionName);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}
