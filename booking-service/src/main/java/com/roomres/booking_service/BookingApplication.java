package com.roomres.booking_service;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class BookingApplication {

	@PostConstruct
	public void init() {
		// A MÁGICA DO FUSO HORÁRIO:
		// Força a máquina virtual do Java a utilizar o horário de Brasília.
		// Assim o LocalDateTime.now() será idêntico ao do usuário no Frontend.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BookingApplication.class, args);
	}
}