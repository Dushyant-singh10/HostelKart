package com.example.TTN_E_Commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TtnECommerceApplication {

	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		SpringApplication.run(TtnECommerceApplication.class, args);
	}

}
