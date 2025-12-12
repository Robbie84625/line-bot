package com.robbie.linebot.font.controller.linebot;


import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import com.robbie.linebot.infra.provider.GeminiAIProvider;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@LineMessageHandler
@AllArgsConstructor
@Slf4j
public class LineBotController {
  private final MessagingApiClient messagingApiClient;
  private final GeminiAIProvider geminiService;

  @EventMapping
  public void handleMessage(MessageEvent event){
    // 要先檢查訊息類型
    if (event.message() instanceof TextMessageContent textContent) {
      String userMessage = textContent.text();
      String replyToken = event.replyToken();

      log.info("收到訊息: {}", userMessage);

      try {
        String response = geminiService.chat(userMessage);

        TextMessage textMessage = new TextMessage(response);

        ReplyMessageRequest request = new ReplyMessageRequest(
            replyToken,
            List.of(textMessage),
            false
        );

        messagingApiClient.replyMessage(request);

      } catch (Exception e) {
        log.error("錯誤", e);
      }
    }
  }
  @EventMapping
  public void handleDefaultEvent(Event event) {
    log.info("收到事件: {}", event);
  }
}
