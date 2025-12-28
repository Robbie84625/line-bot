package com.robbie.linebot.font.api.linebot;

import com.robbie.linebot.infra.provider.GeminiAIProvider;
import com.robbie.linebot.infra.provider.LineBotProvider;
import com.robbie.linebot.infra.support.ErrorReporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiProcessor {
  private final GeminiAIProvider geminiAIProvider;
  private final LineBotProvider lineBotProvider;
  private final ErrorReporter errorReporter;

  @Async
  public void processAsync(ChatFlow.Command command, Runnable onComplete) {
    String userId = command.getUserId();
    String replyToken = command.getReplyToken();
    long processStart = System.currentTimeMillis();
    log.info("[階段 2: 開始處理] 排隊耗時: {} ms", (processStart - command.getArrivalTimestamp()));

    String finalResponse;

    try {
      log.info("[背景處理] 開始為使用者 {} 運算 AI 回應", userId);

      // 呼叫 Gemini Provider (耗時操作)
      long apiStart = System.currentTimeMillis();
      finalResponse = geminiAIProvider.chat(command.getMessage());
      log.info("[階段 3: AI 運算完成] AI 耗時: {} ms", (System.currentTimeMillis() - apiStart));

    } catch (Exception e) {
      errorReporter.report("Gemini AI 運算失敗", e);
      finalResponse = "慢慢說，聽不懂";
    }

    // 回覆 LINE
    try {
      lineBotProvider.replyToLine(replyToken, finalResponse);
    } catch (Exception e) {
      // 5. 如果連發送 LINE 都失敗，那就報警，這時候已經沒辦法回覆使用者了
      errorReporter.report("LINE 訊息回覆失敗", e);
    } finally {
      // 釋放使用者忙碌狀態，讓該使用者可以進行下一次對話
      if (onComplete != null) onComplete.run();
    }
  }
}
