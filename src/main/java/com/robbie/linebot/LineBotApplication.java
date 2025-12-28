package com.robbie.linebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LineBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(LineBotApplication.class, args);
  }

}
