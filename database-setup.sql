-- MS SQL Server Database Setup Script for SLIIT EMS
-- Run this script to create the database and configure it for the application

-- Create the database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'emsdb')
BEGIN
    CREATE DATABASE emsdb;
END
GO

-- Use the database
USE emsdb;
GO

-- Enable SQL Server authentication if needed
-- ALTER LOGIN sa ENABLE;
-- ALTER LOGIN sa WITH PASSWORD = 'YourPassword123!';

-- Grant necessary permissions
-- GRANT CREATE TABLE TO sa;
-- GRANT CREATE PROCEDURE TO sa;
-- GRANT CREATE VIEW TO sa;

PRINT 'Database emsdb created successfully!';
PRINT 'Connection String: jdbc:sqlserver://localhost:1433;databaseName=emsdb;encrypt=true;trustServerCertificate=true';
PRINT 'Username: sa';
PRINT 'Password: YourPassword123!';
PRINT '';
PRINT 'The application will automatically create all tables and relationships when it starts.';
PRINT 'Make sure SQL Server is running on port 1433 and the sa account is enabled.';
