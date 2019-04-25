package com.iphayao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.iphayao.linebot.foodsController;
import com.iphayao.linebot.holidayController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class LineApplication {

  public static Path downloadedContentDir;
	
	public static void main(String[] args) throws IOException {
	downloadedContentDir = Files.createTempDirectory("line-bot");
	SpringApplication.run(LineApplication.class, args);
	holidayController holiday = new holidayController();
	holiday.getClass();
	foodsController foods = new foodsController();
	foods.getClass();

	
	}
}
