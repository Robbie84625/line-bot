package com.robbie.linebot.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatTask {
  private String messageId;
  private String userId;
  private String replyToken;
  private String message;
  private long arrivalTimestamp;
}
