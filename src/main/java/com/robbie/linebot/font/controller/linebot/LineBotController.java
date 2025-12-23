package com.robbie.linebot.font.controller.linebot;

import com.linecorp.bot.parser.LineSignatureValidator;
import com.linecorp.bot.parser.WebhookParser;
import com.linecorp.bot.webhook.model.CallbackRequest;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import com.robbie.linebot.font.api.linebot.ChatPresentation;
import com.robbie.linebot.font.api.linebot.model.ChatRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LineBotController {
  private final ChatPresentation chatPresentation;

  @Value("${line.bot.channel-secret}")
  private String channelSecret;

  @PostMapping("/callback")
  public ResponseEntity<String> callback(
      @RequestHeader("X-Line-Signature") String signature, @RequestBody String payload) {
    try {
      // 驗證簽章 (這在大型專案中非常重要，確保請求真的來自 LINE)
      LineSignatureValidator validator = new LineSignatureValidator(channelSecret.getBytes());
      WebhookParser parser = new WebhookParser(validator);

      // 解析 Payload (注意：新版傳入的是 byte[])
      CallbackRequest callbackRequest = parser.handle(signature, payload.getBytes());

      // 3. 處理事件
      for (Event event : callbackRequest.events()) {
        // 使用新版的 Java Pattern Matching (if instanceof) 判斷更簡潔
        if (event instanceof MessageEvent messageEvent
            && messageEvent.message() instanceof TextMessageContent textContent) {
          ChatRequest request =
              ChatRequest.builder()
                  .messageId(messageEvent.message().id())
                  .userId(messageEvent.source().userId())
                  .message(textContent.text())
                  .replyToken(messageEvent.replyToken())
                  .build();

          chatPresentation.sendChatMessage(request);
        }
      }
      return ResponseEntity.ok("OK");
    } catch (Exception e) {
      log.error("Webhook 解析失敗: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
