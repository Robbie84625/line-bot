package com.robbie.linebot.infra.provider;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.ShowLoadingAnimationRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LineBotProvider {

    private final MessagingApiClient messagingApiClient;

    public void showLoading(String userId, int seconds){
        try {
            messagingApiClient.showLoadingAnimation(
                    new ShowLoadingAnimationRequest(userId, seconds)
            );
        } catch (Exception e) {
            log.warn("無法顯示 Loading 動畫: {}", e.getMessage());
        }
    }

    public void replyToLine(String replyToken, String text) {
        long callStart = System.currentTimeMillis();
        TextMessage textMessage = new TextMessage(text);
        ReplyMessageRequest request = new ReplyMessageRequest(replyToken, List.of(textMessage), false);
        messagingApiClient.replyMessage(request);
        log.info("[階段 4: LINE API 回傳] 耗時: {} ms", (System.currentTimeMillis() - callStart));
    }
}
