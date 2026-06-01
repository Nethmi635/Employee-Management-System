# EMS CRUD Inventory

This document maps the CRUD-style features in the EMS application as implemented in the current codebase snapshot.

## Scope

Included here:
- Real create, read, update, delete flows.
- Mutating workflow actions such as approve/reject, mark read, activate/deactivate, and permission grants.

Excluded here:
- Pure dashboard, calendar, help, login, and report-only pages that do not manage persisted records directly.

## Module Overview

| Module | Route prefix | Primary entities | CRUD coverage | Main templates |
|---|---|---|---|---|
| Employee management | `/employee` | `EmployeeProfile`, `FullTimeEmployee`, `ContractEmployee`, `EmployeeDocument` | Create, read, update, delete, status toggle, document upload/delete | `employee/list.html`, `employee/new.html`, `employee/edit.html`, `employee/view.html` |
| Admin user management | `/admin/users` | `UserAccount`, `Role`, `Permission`, `UserPermission` | Create, read, update, delete, role management, permission management | `admin/users/list.html`, `admin/users/new.html`, `admin/users/edit.html`, `admin/users/permissions.html` |
| Attendance management | `/attendance` | `AttendanceRecord` | Create, read, update, delete, check-in, check-out, export | `attendance/list.html`, `attendance/new.html`, `attendance/edit.html` |
| Leave requests | `/leave` | `LeaveRequest`, `LeaveBalance` | Create, read, update status, balance creation | `leave/list.html`, `leave/balances.html` |
| Shift management | `/shifts` | `Shift`, `DayShift`, `NightShift`, `ShiftSwapRequest` | Create, read, update, delete, swap request workflow | `shift/list.html` |
| Notifications | `/notifications` | `Notification`, `NotificationSettings` | Create, read, update, delete, mark as read, bulk delete | `notification/list.html`, `notification/new.html`, `notification/edit.html`, `notification/settings.html` |
| Notification settings | `/notifications/settings` | `NotificationSettings` | Read, update | `notification/settings.html` |
| Performance management | `/performance` | `PerformanceReview`, `QuarterlyReview`, `AnnualReview`, `Kpi` | Create, read, update, delete, export | `performance/list.html` |
| Leave balances | `/leave/balances` | `LeaveBalance` | Read | `leave/balances.html` |

## Detailed Breakdown

### Employee Management

- Controller: `src/main/java/com/example/ems/controller/EmployeeController.java`
- Route prefix: `/employee`
- Service layer: `EmployeeService`, `EmployeeValidationService`, `NotificationService`
- Repository dependencies: `UserAccountRepository`
- Templates:
  - `src/main/resources/templates/employee/list.html`
  - `src/main/resources/templates/employee/new.html`
  - `src/main/resources/templates/employee/edit.html`
  - `src/main/resources/templates/employee/view.html`

Supported actions:
- `GET /employee` lists employees with filters for query, active status, department, job title, and employment type.
- `GET /employee/new` opens the create form.
- `POST /employee/new/fulltime` creates a full-time employee.
- `POST /employee/new/contract` creates a contract employee.
- `GET /employee/{id}` shows employee details and documents.
- `GET /employee/{id}/edit` opens the edit form.
- `POST /employee/{id}/edit` updates profile fields and optional photo upload.
- `POST /employee/{id}/activate` and `POST /employee/{id}/deactivate` toggle active status.
- `POST /employee/{id}/documents` uploads an employee document.
- `POST /employee/{id}/documents/{docId}/delete` deletes a specific document.
- `POST /employee/{id}/delete` deletes the employee.

Notes:
- Employee creation is split into full-time and contract flows.
- Security uses permission and role checks for create and list access.
- New employee creation triggers admin notifications.

### Admin User Management

- Controller: `src/main/java/com/example/ems/controller/AdminUserController.java`
- Route prefix: `/admin/users`
- Service layer: `NotificationService`, `PermissionService`
- Repository dependencies: `UserAccountRepository`, `RoleRepository`
- Templates:
  - `src/main/resources/templates/admin/users/list.html`
  - `src/main/resources/templates/admin/users/new.html`
  - `src/main/resources/templates/admin/users/edit.html`
  - `src/main/resources/templates/admin/users/permissions.html`

Supported actions:
- `GET /admin/users` lists all users and available roles.
- `GET /admin/users/new` opens the create form.
- `POST /admin/users` creates a new user account and assigns optional roles.
- `GET /admin/users/{id}/edit` opens the edit form.
- `POST /admin/users/{id}/edit` updates username, password, enabled state, roles, and selected permissions.
- `GET /admin/users/{id}/permissions` opens the per-user permissions form.
- `POST /admin/users/{id}/permissions` updates permissions for a single user.
- `POST /admin/users/{id}/delete` deletes a user.
- `POST /admin/users/roles` creates a role if it does not already exist.
- `POST /admin/users/roles/{id}/delete` deletes a role.
- `GET /admin/users/permissions` shows the permission matrix for all users.
- `POST /admin/users/permissions/grant` grants one permission to one user.
- `POST /admin/users/permissions/revoke` revokes one permission from one user.
- `POST /admin/users/permissions/bulk` replaces or clears permissions in bulk.

Notes:
- This controller is admin-only at the class level.
- Permission updates are handled both during user edit and in dedicated permission endpoints.
- User creation notifies existing admins.

### Attendance Management

- Controller: `src/main/java/com/example/ems/controller/AttendanceController.java`
- Route prefix: `/attendance`
- Service layer: attendance service implementation used by the controller
- Repository dependencies: employee lookup via `EmployeeProfileRepository`
- Templates:
  - `src/main/resources/templates/attendance/list.html`
  - `src/main/resources/templates/attendance/new.html`
  - `src/main/resources/templates/attendance/edit.html`

Supported actions:
- `GET /attendance` lists records, optionally filtered by employee.
- `POST /attendance/checkin` creates a check-in record.
- `POST /attendance/checkout/{id}` records check-out time.
- `POST /attendance/{id}/delete` deletes a record.
- `GET /attendance/new` opens the manual create form.
- `POST /attendance/new` creates a manual attendance record.
- `GET /attendance/{id}/edit` opens the edit form.
- `POST /attendance/{id}/edit` updates an attendance record.
- `GET /attendance/export.csv` exports attendance as CSV.
- `GET /attendance/export.xlsx` exports attendance as XLSX.

Notes:
- This module mixes CRUD with operational time tracking.
- Exports are part of the same controller but are not CRUD themselves.

### Leave Requests

- Controller: `src/main/java/com/example/ems/controller/LeaveController.java`
- Route prefix: `/leave`
- Service layer: `LeaveService`
- Repository dependencies: `EmployeeProfileRepository`
- Templates:
  - `src/main/resources/templates/leave/list.html`
  - `src/main/resources/templates/leave/balances.html`

Supported actions:
- `GET /leave` lists leave requests and supports filters by employee and status.
- `POST /leave` creates a leave request.
- `POST /leave/{id}/status` updates leave status, including approval or rejection comments.
- `POST /leave/balances` creates a leave balance entry for a given employee, leave type, and year.

Notes:
- Status updates are restricted to manager, HR, and admin roles.
- Leave balances are created through the leave controller but displayed in a separate route.

### Leave Balances

- Controller: `src/main/java/com/example/ems/controller/LeaveBalancesController.java`
- Route prefix: `/leave/balances`
- Service layer: `LeaveService`
- Repository dependencies: `EmployeeProfileRepository`
- Template: `src/main/resources/templates/leave/balances.html`

Supported actions:
- `GET /leave/balances` lists balances for a selected employee and year.

Notes:
- This controller is read-only.
- It still belongs in the inventory because it exposes a persisted entity view.

### Shift Management

- Controller: `src/main/java/com/example/ems/controller/ShiftController.java`
- Route prefix: `/shifts`
- Service layer: `ShiftService`
- Repository dependencies: `EmployeeProfileRepository`
- Template: `src/main/resources/templates/shift/list.html`

Supported actions:
- `GET /shifts` lists shifts, approved swaps, and pending swaps.
- `POST /shifts/day` creates a day shift.
- `POST /shifts/night` creates a night shift.
- `POST /shifts/{id}/edit` updates a shift.
- `POST /shifts/{id}/delete` deletes a shift.
- `GET /shifts/{id}/delete` also deletes a shift.
- `POST /shifts/swap-request` creates a swap request.
- `POST /shifts/swap-request/{id}/approve` approves a swap request.
- `POST /shifts/swap-request/{id}/reject` rejects a swap request.
- `POST /shifts/swap-request/{id}/delete` deletes a swap request.
- `GET /shifts/test-swap` creates a test swap request for validation/demo purposes.

Notes:
- Shift creation and swap approval are restricted to manager, HR, and admin roles.
- The controller handles both shift CRUD and the shift-swap workflow.

### Notifications

- Controller: `src/main/java/com/example/ems/controller/NotificationController.java`
- Route prefix: `/notifications`
- Service layer: `NotificationService`
- Repository dependencies: `NotificationRepository`, `UserAccountRepository`
- Templates:
  - `src/main/resources/templates/notification/list.html`
  - `src/main/resources/templates/notification/new.html`
  - `src/main/resources/templates/notification/edit.html`
  - `src/main/resources/templates/notification/settings.html`

Supported actions:
- `GET /notifications` lists notifications with unread and recipient filters.
- `POST /notifications/mark-read` marks a notification as read.
- `POST /notifications/delete` deletes a single notification.
- `POST /notifications/delete-all` deletes all notifications for authorized users.
- `GET /notifications/new` opens the create form.
- `POST /notifications/new` creates a notification.
- `GET /notifications/{id}/edit` opens the edit form.
- `POST /notifications/{id}/edit` updates a notification.
- `GET /notifications/test` creates sample notifications for the current user.

Notes:
- Admin and HR can manage all notifications; other users are limited to their own.
- The controller enforces recipient ownership for mark-read and delete operations.

### Notification Settings

- Controller: `src/main/java/com/example/ems/controller/NotificationSettingsController.java`
- Route prefix: `/notifications/settings`
- Service layer: `NotificationService`
- Repository dependencies: `UserAccountRepository`
- Template: `src/main/resources/templates/notification/settings.html`

Supported actions:
- `GET /notifications/settings` loads the current user's settings.
- `POST /notifications/settings` updates the current user's notification preferences.

Notes:
- The settings payload includes delivery-channel toggles and contact details.

### Performance Management

- Controller: `src/main/java/com/example/ems/controller/PerformanceController.java`
- Route prefix: `/performance`
- Service layer: `PerformanceService`
- Repository dependencies: `EmployeeProfileRepository`
- Template: `src/main/resources/templates/performance/list.html`

Supported actions:
- `GET /performance` lists reviews and KPIs, optionally filtered by employee.
- `POST /performance/quarterly` creates a quarterly review.
- `POST /performance/annual` creates an annual review.
- `POST /performance/{id}/edit` updates a review.
- `POST /performance/{id}/delete` deletes a review.
- `POST /performance/kpi` creates a KPI.
- `POST /performance/kpi/{id}/edit` updates a KPI.
- `POST /performance/kpi/{id}/delete` deletes a KPI.
- `GET /performance/export.csv` exports reviews as CSV.
- `GET /performance/export.xlsx` exports reviews as XLSX.

Notes:
- Manager, HR, and admin roles control the mutating endpoints.
- Reviews and KPIs are handled as separate subflows under the same controller.

## Supporting Files And Data Model Hints

These domain packages back the CRUD flows above:
- `src/main/java/com/example/ems/domain/employee/`
- `src/main/java/com/example/ems/domain/leave/`
- `src/main/java/com/example/ems/domain/shift/`
- `src/main/java/com/example/ems/domain/notification/`
- `src/main/java/com/example/ems/domain/performance/`
- `src/main/java/com/example/ems/domain/user/`

And these repositories are the main persistence entry points:
- `src/main/java/com/example/ems/repository/EmployeeProfileRepository.java`
- `src/main/java/com/example/ems/repository/LeaveRequestRepository.java`
- `src/main/java/com/example/ems/repository/LeaveBalanceRepository.java`
- `src/main/java/com/example/ems/repository/ShiftRepository.java`
- `src/main/java/com/example/ems/repository/ShiftSwapRequestRepository.java`
- `src/main/java/com/example/ems/repository/NotificationRepository.java`
- `src/main/java/com/example/ems/repository/NotificationSettingsRepository.java`
- `src/main/java/com/example/ems/repository/PerformanceReviewRepository.java`
- `src/main/java/com/example/ems/repository/QuarterlyReviewRepository.java`
- `src/main/java/com/example/ems/repository/AnnualReviewRepository.java`
- `src/main/java/com/example/ems/repository/KpiRepository.java`
- `src/main/java/com/example/ems/repository/UserAccountRepository.java`
- `src/main/java/com/example/ems/repository/RoleRepository.java`

## Quick Read

If you only need the core CRUD modules, the main ones are:
- Employee management
- Admin user management
- Attendance management
- Leave requests and leave balances
- Shift management and swap requests
- Notifications and notification settings
- Performance management
