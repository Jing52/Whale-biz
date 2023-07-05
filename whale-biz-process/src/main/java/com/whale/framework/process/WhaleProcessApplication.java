package com.whale.framework.process;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class WhaleProcessApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhaleProcessApplication.class, args);
	}

}
