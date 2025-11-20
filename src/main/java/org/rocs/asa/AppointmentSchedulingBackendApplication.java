package org.rocs.asa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  AppointmentSchedulingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentSchedulingBackendApplication.class, args);
	}
}
