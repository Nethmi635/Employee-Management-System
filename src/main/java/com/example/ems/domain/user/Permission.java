package com.example.ems.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "USER_CREATE", "ATTENDANCE_VIEW", "LEAVE_APPROVE"

    @Column(nullable = false)
    private String category; // e.g., "USER_MANAGEMENT", "ATTENDANCE", "LEAVE", "REPORTS"

    @Column(length = 500)
    private String description;

    @Column(name = "is_system_permission")
    private boolean systemPermission = false; // System permissions cannot be deleted

    public Permission(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public Permission(String name, String category, String description, boolean systemPermission) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.systemPermission = systemPermission;
    }
}
