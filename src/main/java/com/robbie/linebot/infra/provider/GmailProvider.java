package com.robbie.linebot.infra.provider;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GmailProvider {

  private final JavaMailSender javaMailSender;

  @Value("${app.admin.email}")
  private String adminEmail;

  public void sendError(String location, Exception e) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(adminEmail);
      message.setSubject("[ERROR] " + location);
      message.setText(
          String.format(
              "錯誤位置: %s\n錯誤類型: %s\n錯誤訊息: %s\n時間: %s",
              location, e.getClass().getSimpleName(), e.getMessage(), LocalDateTime.now()));
      javaMailSender.send(message);
    } catch (Exception ex) {
      log.error("發信失敗", ex);
    }
  }
}
