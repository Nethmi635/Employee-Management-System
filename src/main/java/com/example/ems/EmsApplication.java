package com.example.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmsApplication {

	public static void main(String[] args) {
		/*
		 Default users
		 - In-memory (Spring Security):
		   admin/admin123 (ROLE_ADMIN)
		   hr/hr123 (ROLE_HR)
		   manager/manager123 (ROLE_MANAGER)
		   employee/emp123 (ROLE_EMPLOYEE)

		 - Database (H2, seeded on first run):
		   db-admin/admin123 (ROLE_ADMIN)
		   db-hr/hr123 (ROLE_HR)
		   db-manager/manager123 (ROLE_MANAGER)
		   db-employee/emp123 (ROLE_EMPLOYEE)
		*/
		SpringApplication.run(EmsApplication.class, args);
	}
}


