package com.robbie.linebot.font.controller.linebot;

import com.robbie.linebot.font.api.linebot.ChatFlow;
import com.robbie.linebot.font.api.linebot.GeminiProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/tasks")
public class TaskWorkerController {
  private final GeminiProcessor geminiProcessor;

  @PostMapping("/process-ai")
  public ResponseEntity<Void> handleTask(@RequestBody ChatFlow.Command command) {

    /// Cloud Tasks 呼叫這裡，我們交給 Processor 執行
    geminiProcessor.process(command);

    return ResponseEntity.ok().build();
  }

}
