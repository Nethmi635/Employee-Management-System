package com.example.ems.repository;

import com.example.ems.domain.user.UserAccount;
import com.example.ems.domain.user.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    
    List<UserPermission> findByUserAndActiveTrue(UserAccount user);
    
    List<UserPermission> findByUser(UserAccount user);
    
    @Query("SELECT up FROM UserPermission up WHERE up.user = :user AND up.active = true AND (up.expiresAt IS NULL OR up.expiresAt > :now)")
    List<UserPermission> findEffectivePermissionsByUser(@Param("user") UserAccount user, @Param("now") LocalDateTime now);
    
    @Query("SELECT up.permission.name FROM UserPermission up WHERE up.user = :user AND up.active = true AND (up.expiresAt IS NULL OR up.expiresAt > :now)")
    List<String> findEffectivePermissionNamesByUser(@Param("user") UserAccount user, @Param("now") LocalDateTime now);
    
    Optional<UserPermission> findByUserAndPermission(UserAccount user, com.example.ems.domain.user.Permission permission);
    
    @Query("SELECT up FROM UserPermission up WHERE up.user = :user AND up.permission.name = :permissionName")
    Optional<UserPermission> findByUserAndPermissionName(@Param("user") UserAccount user, @Param("permissionName") String permissionName);
    
    void deleteByUserAndPermission(UserAccount user, com.example.ems.domain.user.Permission permission);
    
    @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.permission = :permission AND up.active = true")
    long countActiveUsersWithPermission(@Param("permission") com.example.ems.domain.user.Permission permission);
}
