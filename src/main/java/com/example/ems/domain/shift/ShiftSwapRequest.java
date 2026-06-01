package com.example.ems.domain.shift;

import com.example.ems.domain.employee.EmployeeProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ShiftSwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Shift requestedShift;

    @ManyToOne(optional = false)
    private Shift offeredShift;

    @ManyToOne(optional = false)
    private EmployeeProfile requester;

    @ManyToOne(optional = false)
    private EmployeeProfile targetEmployee;

    private String reason;
    @Column(name = "requested_at")
    private LocalDateTime requestedAt = LocalDateTime.now();
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isRejected() {
        return status == Status.REJECTED;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    public void approve() {
        this.status = Status.APPROVED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = Status.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}
