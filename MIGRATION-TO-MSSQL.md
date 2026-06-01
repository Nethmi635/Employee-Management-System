# Migration from H2 to MS SQL Server

This document outlines the changes made to migrate the SLIIT EMS system from H2 database to MS SQL Server.

## Changes Made

### 1. Maven Dependencies (pom.xml)
- **Removed**: H2 database dependency completely
- **Added**: Microsoft SQL Server JDBC driver dependency
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Database Configuration (application.properties)
- **Updated**: Database connection URL to MS SQL Server
- **Updated**: Driver class name to SQL Server driver
- **Updated**: Hibernate dialect to SQL Server dialect
- **Removed**: H2 console configuration
- **Added**: SQL Server specific configurations

### 3. Security Configuration (SecurityConfig.java)
- **Removed**: H2 console access permissions completely
- **Removed**: H2 console CSRF exemptions completely

### 4. Data Loader (DataLoader.java)
- **Updated**: Log messages to reflect MS SQL Server connection info only
- **Removed**: All H2 database references

## Database Setup

### Prerequisites
1. Install MS SQL Server (2019 or later recommended)
2. Enable SQL Server Authentication
3. Create a user account with appropriate permissions

### Database Creation
Run the provided `database-setup.sql` script to create the database:

```sql
-- The script will create the 'emsdb' database
-- Make sure to update the password in the script if needed
```

### Connection Configuration
The application is configured to connect to:
- **Server**: localhost:1433
- **Database**: emsdb
- **Username**: sa
- **Password**: YourPassword123!

**Important**: Update the password in `application.properties` to match your SQL Server configuration.

## Schema Compatibility

All existing entity classes are compatible with MS SQL Server:
- ✅ Inheritance strategies (SINGLE_TABLE)
- ✅ Discriminator columns
- ✅ Enum mappings
- ✅ Date/Time fields (LocalDate, LocalDateTime)
- ✅ Relationships (OneToMany, ManyToOne, ManyToMany)
- ✅ Constraints (unique, nullable)

## Tables Created Automatically

The application will automatically create the following tables when it starts:
- `user_account` (with discriminator column)
- `role`
- `user_roles` (join table)
- `employee_profile` (with discriminator column)
- `attendance_record`
- `leave_request`
- `leave_balance`
- `shift` (with discriminator column)
- `shift_swap_request`
- `performance_review` (with discriminator column)
- `kpi`
- `notification`
- `notification_settings`
- `employee_document`
- `password_reset_token`

## Data Migration

The application includes comprehensive data seeding that will:
- Create default user roles (ADMIN, HR, MANAGER, EMPLOYEE)
- Create sample users for testing
- Create sample employee profiles
- Create sample attendance records
- Create sample leave requests and balances
- Create sample performance reviews
- Create sample shifts
- Create sample notifications

## Testing the Migration

1. Install and start MS SQL Server
2. Run the database setup script (`database-setup.sql`)
3. Update the password in `application.properties` if needed
4. Start the application: `mvn spring-boot:run`
5. Access the application at: http://localhost:8080
6. Login with any of the default credentials

**Note**: The application now runs exclusively with MS SQL Server. No H2 fallback is available.

## Default Users

The following users are created automatically:
- admin / admin123 (ROLE_ADMIN)
- hr / hr123 (ROLE_HR)
- manager / manager123 (ROLE_MANAGER)
- employee / emp123 (ROLE_EMPLOYEE)
- dinaya / dinaya123 (ROLE_ADMIN)
- sadeni / sadeni123 (ROLE_HR)
- nethmi / nethmi123 (ROLE_MANAGER)
- tharushi / tharushi123 (ROLE_EMPLOYEE)
- hiruni / hiruni123 (ROLE_EMPLOYEE)

## Troubleshooting

### Connection Issues
- Verify SQL Server is running on port 1433
- Check firewall settings
- Ensure SQL Server Authentication is enabled
- Verify username/password in application.properties

### Schema Issues
- The application uses `hibernate.hbm2ddl.auto=update` to automatically create/update tables
- Check SQL Server logs for any constraint violations
- Ensure the database user has CREATE TABLE permissions

### Performance
- Consider adding indexes for frequently queried columns
- Monitor query performance in SQL Server Management Studio
- Adjust connection pool settings if needed

## Rollback

To rollback to H2:
1. Revert changes in `pom.xml`
2. Revert changes in `application.properties`
3. Revert changes in `SecurityConfig.java`
4. Revert changes in `DataLoader.java`
5. Remove the database setup files
