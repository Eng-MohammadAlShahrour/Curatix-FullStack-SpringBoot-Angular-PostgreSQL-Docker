package com.curatix.museumloan;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAsync
@SpringBootApplication
public class MuseumLoanApplication {

	public static void main(String[] args) {
		SpringApplication.run(MuseumLoanApplication.class, args);
	}

}
