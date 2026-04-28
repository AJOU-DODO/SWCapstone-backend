package com.dodo.dodoserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DodoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DodoServerApplication.class, args);
	}

}
