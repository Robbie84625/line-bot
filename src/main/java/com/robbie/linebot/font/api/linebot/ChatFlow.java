package com.robbie.linebot.font.api.linebot;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.robbie.linebot.infra.provider.GeminiAIProvider;
import com.robbie.linebot.infra.provider.GmailProvider;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatFlow {

  private final MessagingApiClient messagingApiClient;
  private final GeminiAIProvider geminiAIProvider;
  private final GmailProvider gmailProvider;

  private final ConcurrentHashMap<String, Long> processedMessages = new ConcurrentHashMap<>();

  public void execute(Command command) {
    String message = command.getMessage();
    String messageId = command.getMessageId();

    if (isDuplicate(messageId)) {
      log.warn("重複的訊息,忽略: {}", messageId);
      return;
    }

    log.info("處理訊息 - ID: {}, 內容: {}", messageId, message);

    try {
      // 步驟1: 呼叫 Gemini API 取得回應
      String geminiAPIResponse = callGeminiAPI(message);

      // 步驟2: 回覆 LINE 訊息
      replyToLine(command.getReplyToken(), geminiAPIResponse);

    } catch (Exception e) {
      log.error("系統錯誤", e);
    }
  }

  /**
   * 檢查是否為重複訊息
   */
  private boolean isDuplicate(String messageId) {
    long now = System.currentTimeMillis();
    Long processedTime = processedMessages.putIfAbsent(messageId, now);

    if (processedTime != null) {
      log.warn("重複訊息! ID: {}, 已於 {} ms 前處理",
          messageId, (now - processedTime));
      return true;
    }

    // 定期清理舊記錄
    if (processedMessages.size() > 1000) {
      long fiveMinutesAgo = now - (5 * 60 * 1000);
      processedMessages.entrySet().removeIf(entry ->
          entry.getValue() < fiveMinutesAgo
      );
    }

    return false;
  }

  private String callGeminiAPI(String message) throws Exception {
    try {
      return geminiAIProvider.chat(message);
    } catch (Exception e) {
      log.error("Gemini API 呼叫失敗", e);
      gmailProvider.sendError("Gemini API 呼叫失敗", e);
      throw e;
    }
  }

  private void replyToLine(String replyToken,String message) {
    try{
      TextMessage textMessage = new TextMessage(message);
      ReplyMessageRequest request =
          new ReplyMessageRequest(replyToken, List.of(textMessage), false);
      messagingApiClient.replyMessage(request);
    }catch (Exception e){
      log.error("LINE 訊息回覆失敗", e);
      gmailProvider.sendError("LINE 訊息回覆失敗", e);
      throw e;
    }
  }

  @Getter
  @Builder
  public static class Command {
    private final String messageId;
    private String userId;
    private String replyToken;
    private String message;
  }
}
