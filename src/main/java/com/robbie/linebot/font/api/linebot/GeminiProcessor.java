package com.robbie.linebot.font.api.linebot;

import com.github.benmanes.caffeine.cache.Cache;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.robbie.linebot.infra.provider.GeminiAIProvider;
import com.robbie.linebot.infra.provider.GmailProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiProcessor {
  private final GeminiAIProvider geminiAIProvider;
  private final MessagingApiClient messagingApiClient;
  private final GmailProvider gmailProvider;

  @Async("geminiExecutor")
  public void processAsync(ChatFlow.Command command, Cache<String, Boolean> userBusyLock) {
    String userId = command.getUserId();
    String replyToken = command.getReplyToken();

    try {
      log.info("[背景處理] 開始為使用者 {} 運算 AI 回應", userId);

      // 1. 呼叫 Gemini Provider (耗時操作)
      long apiStart = System.currentTimeMillis();
      String aiResponse = geminiAIProvider.chat(command.getMessage());
      log.info("Gemini 回應耗時: {} ms", (System.currentTimeMillis() - apiStart));

      // 2. 回覆 Line 訊息
      replyToLine(replyToken, aiResponse);

    } catch (Exception e) {
      log.error("[背景處理出錯] User: {}", userId, e);
      replyToLine(replyToken, "慢慢說，聽不懂");
      gmailProvider.sendError("Gemini Process Error", e);
    } finally {
      // 3. 釋放使用者忙碌狀態，讓該使用者可以進行下一次對話
      userBusyLock.invalidate(userId);
      log.info("[背景處理完成] 已釋放使用者 {} 的鎖定", userId);
    }
  }

  private void replyToLine(String replyToken, String text) {
    try {
      TextMessage textMessage = new TextMessage(text);
      ReplyMessageRequest request =
          new ReplyMessageRequest(replyToken, List.of(textMessage), false);
      messagingApiClient.replyMessage(request);
    } catch (Exception e) {
      log.error("LINE 訊息回覆失敗: {}", e.getMessage());
      gmailProvider.sendError("LINE 訊息回覆失敗", e);
    }
  }
}
