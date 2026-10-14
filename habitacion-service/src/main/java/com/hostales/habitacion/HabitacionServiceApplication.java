package com.hostales.habitacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HabitacionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitacionServiceApplication.class, args);
	}
}
