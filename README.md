# Employee Management System

This repository contains a Spring Boot-based Employee Management System web application.

**Prerequisites:**
- Java 17 or later installed and `JAVA_HOME` set.
- Maven 3.6+ installed.
- A running database if you use an external datasource (MySQL example shown below).

## Quick start

1. Build the project:

	`mvn clean package`

2. Run with Maven (development):

	`mvn spring-boot:run`

3. Or run the produced jar (after `mvn package`):

	`java -jar target/ems-0.0.1-SNAPSHOT.jar`

The application runs by default on port 8080. Open `http://localhost:8080` in your browser.

## Configuration

- Application properties are in `src/main/resources/application.properties`.
- To use MySQL, update `src/main/resources/application.properties` with values like:

```
spring.datasource.url=jdbc:mysql://localhost:3306/ems_db
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

- To change the server port, set `server.port=9090` (or any other free port) in `application.properties`.

## Run from your IDE

- Import the project as a Maven project.
- Run the `main` method in the Spring Boot application class (named `EmsApplication` in `src/main/java`).

## Tests

Run the test suite with:

`mvn test`

## Troubleshooting

- If the app cannot connect to the database, check `application.properties` and ensure the database is accessible.
- Check logs in the console for stack traces when starting the application.
