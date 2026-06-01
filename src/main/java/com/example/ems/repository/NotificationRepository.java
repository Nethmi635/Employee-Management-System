package com.example.ems.repository;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByRecipientIdAndReadFalse(Long userId);
	List<Notification> findByRecipient(UserAccount recipient);
	List<Notification> findByRecipientAndReadFalse(UserAccount recipient);
	List<Notification> findByRecipientOrderByCreatedAtDesc(UserAccount recipient);

	// Admin views
	List<Notification> findAllByOrderByCreatedAtDesc();
	List<Notification> findByReadFalseOrderByCreatedAtDesc();
	long countByReadFalse();
}
