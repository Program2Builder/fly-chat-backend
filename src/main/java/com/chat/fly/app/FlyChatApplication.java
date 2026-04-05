package com.chat.fly.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FlyChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlyChatApplication.class, args);
	}

}
