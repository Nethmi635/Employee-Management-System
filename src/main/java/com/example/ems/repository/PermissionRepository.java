package com.example.ems.repository;

import com.example.ems.domain.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByName(String name);
    
    List<Permission> findByCategory(String category);
    
    List<Permission> findBySystemPermission(boolean systemPermission);
    
    @Query("SELECT DISTINCT p.category FROM Permission p ORDER BY p.category")
    List<String> findDistinctCategories();
    
    boolean existsByName(String name);
}
