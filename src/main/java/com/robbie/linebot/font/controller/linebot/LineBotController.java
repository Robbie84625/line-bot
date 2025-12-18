package com.robbie.linebot.font.controller.linebot;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import com.robbie.linebot.font.api.linebot.ChatPresentation;
import com.robbie.linebot.font.api.linebot.model.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@LineMessageHandler
@Component
@AllArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class LineBotController {
  //  private final MessagingApiClient messagingApiClient;
  //  private final GeminiAIProvider geminiService;

  private final ChatPresentation chatPresentation;

  @EventMapping
  public void handleMessage(MessageEvent event) {
    // 要先檢查訊息類型
    if (event.message() instanceof TextMessageContent textContent) {
      String userMessage = textContent.text();
      String replyToken = event.replyToken();

      ChatRequest request =
          ChatRequest.builder()
              .userId(event.source().userId())
              .message(textContent.text())
              .replyToken(event.replyToken())
              .build();

      chatPresentation.sendChatMessage(request);
    }
  }

  @EventMapping
  public void handleDefaultEvent(Event event) {
    log.info("收到事件: {}", event);
  }
}
