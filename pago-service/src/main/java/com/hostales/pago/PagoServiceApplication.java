package com.hostales.pago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PagoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoServiceApplication.class, args);
    }
}
