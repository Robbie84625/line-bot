package com.robbie.linebot.font.api.linebot;

import com.robbie.linebot.font.api.linebot.model.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPresentation {
  private final ChatFlow chatFlow;

  public void sendChatMessage(ChatRequest chatRequest) {

    ChatFlow.Command command =
        ChatFlow.Command.builder()
            .userId(chatRequest.getUserId())
            .replyToken(chatRequest.getReplyToken())
            .messageId(chatRequest.getMessageId())
            .message(chatRequest.getMessage())
            .arrivalTimestamp(chatRequest.getArrivalTimestamp())
            .build();

    chatFlow.execute(command);
  }
}
