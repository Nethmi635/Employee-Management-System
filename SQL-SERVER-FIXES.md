# SQL Server Compatibility Fixes

## Issues Fixed

### 1. Reserved Keyword Issues
The following entities had reserved keyword conflicts with SQL Server:

#### Notification Entity
- **Issue**: `read` is a reserved keyword in SQL Server
- **Fix**: Added `@Column(name = "is_read")` annotation
- **Additional Fixes**: Added column annotations for all date fields

#### UserAccount Entity
- **Issue**: Multiple fields without proper column naming
- **Fix**: Added `@Column` annotations for all security fields

#### EmployeeProfile Entity
- **Issue**: Field names without proper column mapping
- **Fix**: Added `@Column` annotations for name fields and job title

#### AdminUser Entity
- **Issue**: Missing import for `@Column` annotation
- **Fix**: Added import and column annotations

#### PasswordResetToken Entity
- **Issue**: Date fields without proper column naming
- **Fix**: Added `@Column` annotations for date fields

## Changes Made

### Notification.java
```java
@Column(name = "is_read")
private boolean read;

@Column(name = "created_at")
private LocalDateTime createdAt = LocalDateTime.now();

@Column(name = "read_at")
private LocalDateTime readAt;

@Column(name = "expires_at")
private LocalDateTime expiresAt;

@Column(name = "alert_type")
private AlertType alertType = AlertType.SYSTEM;

@Column(name = "delivery_method")
private DeliveryMethod deliveryMethod = DeliveryMethod.IN_APP;
```

### UserAccount.java
```java
@Column(name = "last_login")
private LocalDateTime lastLogin;

@Column(name = "account_created")
private LocalDateTime accountCreated = LocalDateTime.now();

@Column(name = "failed_login_attempts")
private int failedLoginAttempts = 0;

@Column(name = "account_locked_until")
private LocalDateTime accountLockedUntil;

@Column(name = "account_non_expired")
private boolean accountNonExpired = true;

@Column(name = "credentials_non_expired")
private boolean credentialsNonExpired = true;

@Column(name = "account_non_locked")
private boolean accountNonLocked = true;
```

### EmployeeProfile.java
```java
@Column(name = "first_name")
private String firstName;

@Column(name = "last_name")
private String lastName;

@Column(name = "job_title")
private String jobTitle;

@Column(name = "photo_filename")
private String photoFilename;
```

### AdminUser.java
```java
@Column(name = "last_admin_action")
private LocalDateTime lastAdminAction;

@Column(name = "admin_actions_count")
private int adminActionsCount = 0;

@Column(name = "super_admin")
private boolean superAdmin = false;
```

### PasswordResetToken.java
```java
@Column(name = "expiry_date")
private LocalDateTime expiryDate;

@Column(name = "created_at")
private LocalDateTime createdAt = LocalDateTime.now();
```

## Testing Instructions

### Prerequisites
1. **MS SQL Server** must be installed and running
2. **Database** `emsdb` must be created
3. **User** `sa` with password `YourPassword123!` must be configured

### Database Setup
Run the following SQL script to create the database:

```sql
-- Create the database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'emsdb')
BEGIN
    CREATE DATABASE emsdb;
END
GO

-- Use the database
USE emsdb;
GO
```

### Running the Application
1. **Update password** in `application.properties` if your SQL Server password is different
2. **Start the application**: `mvn spring-boot:run`
3. **Access**: http://localhost:8080
4. **Login** with any of the default credentials

### Expected Behavior
- ✅ Application should start without SQL syntax errors
- ✅ All tables should be created successfully
- ✅ Sample data should be seeded automatically
- ✅ All endpoints should be accessible

### Default Users
- admin / admin123 (ROLE_ADMIN)
- hr / hr123 (ROLE_HR)
- manager / manager123 (ROLE_MANAGER)
- employee / emp123 (ROLE_EMPLOYEE)

## Troubleshooting

### Connection Issues
- Verify SQL Server is running on port 1433
- Check firewall settings
- Ensure SQL Server Authentication is enabled
- Verify username/password in application.properties

### Table Creation Issues
- Check SQL Server logs for detailed error messages
- Ensure the database user has CREATE TABLE permissions
- Verify the database exists and is accessible

### Reserved Keyword Issues
- All reserved keyword conflicts have been resolved
- If new issues arise, add `@Column(name = "custom_name")` annotations

## Success Indicators
- ✅ Compilation successful
- ✅ Application starts without errors
- ✅ Database connection established
- ✅ Tables created successfully
- ✅ Sample data seeded
- ✅ Web interface accessible
