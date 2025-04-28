package org.db.hrsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class StaffingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaffingApplication.class, args);
	}

}
