package com.robbie.linebot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class LineBotApplicationTests {

  @Autowired
  private JavaMailSender javaMailSender;

  @Test
  void contextLoads() {
  }

  @Test
  public void sendTestEmail() {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo("loby84625@gmail.com"); //設置收件人信箱
    message.setSubject("Test Email"); //設置信箱主題
    message.setText("This is a test email."); //設置信箱內容
    javaMailSender.send(message); //發送郵件
  }

}
