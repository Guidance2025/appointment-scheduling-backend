package org.rocs.asa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class  AppointmentSchedulingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentSchedulingBackendApplication.class, args);
	}
}
