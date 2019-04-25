package com.iphayao.linebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.iphayao.LineApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(LineApplication.class, args);

	}
}
