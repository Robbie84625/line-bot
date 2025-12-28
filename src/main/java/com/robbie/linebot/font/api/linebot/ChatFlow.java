package com.robbie.linebot.font.api.linebot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import com.robbie.linebot.infra.provider.LineBotProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatFlow {
  private final GeminiProcessor geminiProcessor;
  private final LineBotProvider lineBotProvider;

  // 10秒內重複的訊息 ID 直接擋掉 (防止 Line 重試)
  private final Cache<String, Boolean> duplicateLock =
      Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

  // 5秒內同一個使用者不能發兩次 (防止刷訊息)
  private final Cache<String, Boolean> userBusyLock =
      Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

  public void execute(Command command) {
    long startTime = System.currentTimeMillis();
    log.info("Webhook 進入 Controller: {} ms", startTime);

    String msgId = command.getMessageId();
    String userId = command.getUserId();

    // 1. 去重檢查
    if (duplicateLock.getIfPresent(msgId) != null) {
      log.warn("[去重] 訊息 ID {} 重複，跳過", msgId);
      return;
    }
    duplicateLock.put(msgId, true);

    // 2. 限流檢查
    if (userBusyLock.getIfPresent(userId) != null) {
      log.warn("[限流] 使用者 {} 正在輸入中，跳過新請求", userId);
      return;
    }

    userBusyLock.put(userId, true);

    // 假如在思考顯示動畫給使用者
    lineBotProvider.showLoading(userId,30);

    // 3. 任務交辦 (交給 Processor 在背景跑)
    // 這裡會立刻回傳，不等待 AI 結果
    geminiProcessor.processAsync(command, () -> {
      userBusyLock.invalidate(userId);
      log.info("[鎖釋放] 使用者 {} 已可再次輸入", userId);
    });

    log.info("[任務已派發] User: {}, MsgId: {}", userId, msgId);
  }

  @Getter
  @Builder
  public static class Command {
    private final String messageId;
    private String userId;
    private String replyToken;
    private String message;
    private  long arrivalTimestamp;
  }
}
