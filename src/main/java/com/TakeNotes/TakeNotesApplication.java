package com.TakeNotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TakeNotesApplication {
	public static void main(String[] args) {
		SpringApplication.run(TakeNotesApplication.class, args);
	}
}
