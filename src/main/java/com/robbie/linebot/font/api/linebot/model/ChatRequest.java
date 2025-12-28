package com.robbie.linebot.font.api.linebot.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRequest {
  private String messageId;
  private String userId;
  private String replyToken;
  private String message;
  private long arrivalTimestamp;
}
