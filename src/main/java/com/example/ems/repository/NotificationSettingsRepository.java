package com.example.ems.repository;

import com.example.ems.domain.notification.NotificationSettings;
import com.example.ems.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    Optional<NotificationSettings> findByUser(UserAccount user);
    Optional<NotificationSettings> findByUserId(Long userId);
}
