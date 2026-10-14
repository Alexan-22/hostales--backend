package com.hostales.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ReservaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservaServiceApplication.class, args);
    }
}
