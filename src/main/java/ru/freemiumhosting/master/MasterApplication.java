package ru.freemiumhosting.master;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;

@SpringBootApplication
public class MasterApplication {
	public static void main(String[] args) {
		SpringApplication.run(MasterApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	@SneakyThrows
	public void doSomethingAfterStartup() {
		System.out.println("hello world, I have just started up");
		Runtime.getRuntime().exec("/kaniko/executor ").waitFor();
		System.out.println("123");
	}
}
