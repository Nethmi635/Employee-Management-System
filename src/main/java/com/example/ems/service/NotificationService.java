package com.example.ems.service;

import com.example.ems.domain.notification.Notification;
import com.example.ems.domain.notification.NotificationSettings;
import com.example.ems.domain.user.UserAccount;
import com.example.ems.repository.NotificationRepository;
import com.example.ems.repository.NotificationSettingsRepository;
import com.example.ems.service.notification.DeliveryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationSettingsRepository settingsRepository;
	private final List<DeliveryHandler> deliveryHandlers;

	public NotificationService(NotificationRepository notificationRepository,
	                          NotificationSettingsRepository settingsRepository,
	                          List<DeliveryHandler> deliveryHandlers) {
		this.notificationRepository = notificationRepository;
		this.settingsRepository = settingsRepository;
		this.deliveryHandlers = deliveryHandlers;
	}

	// Enhanced notification creation with alert types and priorities
	public Notification createNotification(UserAccount recipient, String title, String message,
	                                     Notification.AlertType alertType, Notification.Priority priority,
	                                     Notification.DeliveryMethod deliveryMethod) {
		Notification notification = new Notification();
		notification.setRecipient(recipient);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setAlertType(alertType);
		notification.setPriority(priority);
		notification.setDeliveryMethod(deliveryMethod);
		notification.setRead(false);
		
		// Set expiration based on priority
		notification.setExpiresAt(calculateExpiration(priority));
		
		Notification saved = notificationRepository.save(notification);
		
		// Deliver notification using appropriate handler
		deliverNotification(saved, recipient);
		
		return saved;
	}

	// Legacy method for backward compatibility
	public Notification notifyInApp(UserAccount recipient, String message) {
		return createNotification(recipient, "System Notification", message,
			Notification.AlertType.SYSTEM, Notification.Priority.MEDIUM, Notification.DeliveryMethod.IN_APP);
	}

	// Automated alert generation methods
	public Notification createApprovalAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.APPROVAL,
			Notification.Priority.HIGH, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createPerformanceAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.PERFORMANCE,
			Notification.Priority.MEDIUM, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createAttendanceAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.ATTENDANCE,
			Notification.Priority.HIGH, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createShiftReminder(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.SHIFT,
			Notification.Priority.MEDIUM, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createLeaveAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.LEAVE,
			Notification.Priority.MEDIUM, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createSystemAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.SYSTEM,
			Notification.Priority.LOW, Notification.DeliveryMethod.IN_APP);
	}

	public Notification createSecurityAlert(UserAccount recipient, String title, String message) {
		return createNotification(recipient, title, message, Notification.AlertType.SECURITY,
			Notification.Priority.URGENT, Notification.DeliveryMethod.IN_APP);
	}

	// Delivery method using polymorphism
	private void deliverNotification(Notification notification, UserAccount recipient) {
		NotificationSettings settings = getOrCreateSettings(recipient);
		
		// Check if delivery is enabled for this alert type and method
		if (settings.isDeliveryEnabled(notification.getAlertType(), notification.getDeliveryMethod())) {
			deliveryHandlers.stream()
				.filter(handler -> handler.canDeliver(notification.getDeliveryMethod()))
				.findFirst()
				.ifPresent(handler -> handler.deliver(notification, recipient));
		}
	}

	private NotificationSettings getOrCreateSettings(UserAccount user) {
		return settingsRepository.findByUser(user)
			.orElseGet(() -> {
				NotificationSettings settings = new NotificationSettings(user);
				return settingsRepository.save(settings);
			});
	}

	private LocalDateTime calculateExpiration(Notification.Priority priority) {
		return switch (priority) {
			case URGENT -> LocalDateTime.now().plusDays(1);
			case HIGH -> LocalDateTime.now().plusDays(3);
			case MEDIUM -> LocalDateTime.now().plusDays(7);
			case LOW -> LocalDateTime.now().plusDays(14);
		};
	}

	// Enhanced CRUD operations
	public void markAsRead(Long id, UserAccount user) {
		Notification notification = notificationRepository.findById(id).orElseThrow();
		if (notification.getRecipient().equals(user)) {
			notification.markAsRead();
			notificationRepository.save(notification);
		}
	}

	public void delete(Long id, UserAccount user) {
		Notification notification = notificationRepository.findById(id).orElseThrow();
		if (notification.getRecipient().equals(user)) {
			notificationRepository.deleteById(id);
		}
	}

	public void deleteExpired() {
		List<Notification> expired = notificationRepository.findAll().stream()
			.filter(Notification::isExpired)
			.collect(Collectors.toList());
		notificationRepository.deleteAll(expired);
	}

	public long getUnreadCount(UserAccount user) {
		return notificationRepository.findByRecipientAndReadFalse(user).size();
	}

	// Analytics and statistics
	public Map<String, Object> getNotificationStatistics(UserAccount user) {
		List<Notification> userNotifications = notificationRepository.findByRecipient(user);
		
		long total = userNotifications.size();
		long unread = userNotifications.stream().filter(n -> !n.isRead()).count();
		long expired = userNotifications.stream().filter(Notification::isExpired).count();
		
		Map<Notification.AlertType, Long> byType = userNotifications.stream()
			.collect(Collectors.groupingBy(Notification::getAlertType, Collectors.counting()));
		
		Map<Notification.Priority, Long> byPriority = userNotifications.stream()
			.collect(Collectors.groupingBy(Notification::getPriority, Collectors.counting()));
		
		return Map.of(
			"total", total,
			"unread", unread,
			"expired", expired,
			"byType", byType,
			"byPriority", byPriority
		);
	}


	// Notification settings management
	public NotificationSettings getSettings(UserAccount user) {
		return getOrCreateSettings(user);
	}

	public NotificationSettings updateSettings(UserAccount user, NotificationSettings settings) {
		settings.setUser(user);
		return settingsRepository.save(settings);
	}
}


