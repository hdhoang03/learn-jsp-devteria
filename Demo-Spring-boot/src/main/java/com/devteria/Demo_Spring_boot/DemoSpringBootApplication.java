package com.devteria.Demo_Spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class DemoSpringBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoSpringBootApplication.class, args);
	}
}
