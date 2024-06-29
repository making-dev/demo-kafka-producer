package com.example;

import org.springframework.boot.SpringApplication;

public class TestDemoKafkaProducerApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoKafkaProducerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
