package com.pico.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.pico.communication.controller.LineBotController;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = { "com.pico.communication.controller","com.pico.communication.service","com.pico.communication.config" })
@Slf4j
//@SpringBootApplication
public class Application extends SpringBootServletInitializer {
//public class Application {
	public static Path downloadedContentDir;

    public static void main(String[] args) throws IOException {
    	log.info("------------------------------------");
    	log.info(Files.createTempDirectory("line-bot").toString());
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	
}
