package com.example.ems.domain.notification;

import com.example.ems.domain.user.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private UserAccount recipient;

	private String message;
	private String title;
	@Column(name = "is_read")
	private boolean read;
	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
	@Column(name = "read_at")
	private LocalDateTime readAt;
	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "alert_type")
	private AlertType alertType = AlertType.SYSTEM;

	@Enumerated(EnumType.STRING)
	private Priority priority = Priority.MEDIUM;

	@Enumerated(EnumType.STRING)
	@Column(name = "delivery_method")
	private DeliveryMethod deliveryMethod = DeliveryMethod.IN_APP;

	public enum AlertType {
		APPROVAL("Approval Required"),
		PERFORMANCE("Performance Review"),
		ATTENDANCE("Attendance Alert"),
		SHIFT("Shift Reminder"),
		LEAVE("Leave Request"),
		SYSTEM("System Update"),
		SECURITY("Security Alert");

		private final String displayName;

		AlertType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum Priority {
		LOW("Low"),
		MEDIUM("Medium"),
		HIGH("High"),
		URGENT("Urgent");

		private final String displayName;

		Priority(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum DeliveryMethod {
		IN_APP("In-App"),
		EMAIL("Email"),
		SMS("SMS"),
		PUSH("Push Notification");

		private final String displayName;

		DeliveryMethod(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public boolean isExpired() {
		return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
	}

	public void markAsRead() {
		this.read = true;
		this.readAt = LocalDateTime.now();
	}

	public String getPriorityColor() {
		return switch (priority) {
			case LOW -> "#6b7280";
			case MEDIUM -> "#3b82f6";
			case HIGH -> "#f59e0b";
			case URGENT -> "#ef4444";
		};
	}

	public String getAlertTypeColor() {
		return switch (alertType) {
			case APPROVAL -> "#3b82f6";
			case PERFORMANCE -> "#10b981";
			case ATTENDANCE -> "#f59e0b";
			case SHIFT -> "#8b5cf6";
			case LEAVE -> "#06b6d4";
			case SYSTEM -> "#6b7280";
			case SECURITY -> "#ef4444";
		};
	}
}


