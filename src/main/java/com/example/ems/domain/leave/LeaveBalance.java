package com.example.ems.domain.leave;

import com.example.ems.domain.employee.EmployeeProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private EmployeeProfile employee;

	@Enumerated(EnumType.STRING)
	@Column(name = "leave_type")
	private LeaveType leaveType;

    private int totalDays;
    private int usedDays;
    private int remainingDays;
    
    @Column(name = "balance_year")
    private int year;
    
    private LocalDate lastUpdated;

    public enum LeaveType {
        VACATION("Vacation"),
        SICK("Sick Leave"),
        PERSONAL("Personal Leave"),
        MATERNITY("Maternity Leave"),
        PATERNITY("Paternity Leave"),
        EMERGENCY("Emergency Leave");

        private final String displayName;

        LeaveType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public LeaveBalance(EmployeeProfile employee, LeaveType leaveType, int totalDays, int year) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.totalDays = totalDays;
        this.usedDays = 0;
        this.remainingDays = totalDays;
        this.year = year;
        this.lastUpdated = LocalDate.now();
    }

    public void updateUsedDays(int usedDays) {
        this.usedDays = usedDays;
        this.remainingDays = this.totalDays - this.usedDays;
        this.lastUpdated = LocalDate.now();
    }

    public boolean hasAvailableDays(int requestedDays) {
        return this.remainingDays >= requestedDays;
    }
}
