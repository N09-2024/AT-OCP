package com.ocp.at;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OcpAtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcpAtBackendApplication.class, args);
	}

}
