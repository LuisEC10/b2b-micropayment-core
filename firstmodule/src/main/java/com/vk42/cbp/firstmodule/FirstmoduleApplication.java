package com.vk42.cbp.firstmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FirstmoduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirstmoduleApplication.class, args);
	}

}
