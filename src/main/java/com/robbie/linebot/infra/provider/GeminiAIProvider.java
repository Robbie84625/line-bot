package com.robbie.linebot.infra.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GeminiAIProvider {
  private final String apiKey;
  private final String systemPrompt;
  private final RestClient restClient;

  public GeminiAIProvider(
      @Value("${gemini.api.key}") String apiKey,
      @Value("${gemini.api.url}") String apiUrl,
      @Value("${gemini.system.prompt}") String systemPrompt) {
    this.apiKey = apiKey;
    this.systemPrompt = systemPrompt;
    this.restClient =
        RestClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  public String chat(String message) {

    // 建立請求物件
    GeminiRequest requestBody =
        new GeminiRequest(
            new GeminiRequest.SystemInstruction(List.of(new GeminiRequest.Part(systemPrompt))),
            List.of(new GeminiRequest.Content("user", List.of(new GeminiRequest.Part(message)))));

    // 發送請求
    GeminiResponse response =
        restClient
            .post()
            .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
            .body(requestBody)
            .retrieve()
            .onStatus(
                HttpStatusCode::is4xxClientError,
                (req, res) -> {
                  log.error("Gemini API 用戶端錯誤: {} - {}", res.getStatusCode(), res.getStatusText());
                  throw new RuntimeException("API_CLIENT_ERROR");
                })
            .onStatus(
                HttpStatusCode::is5xxServerError,
                (req, res) -> {
                  log.error("Gemini API 伺服器錯誤: {}", res.getStatusCode());
                  throw new RuntimeException("API_SERVER_ERROR");
                })
            .body(GeminiResponse.class);

    return extractText(response);
  }

  private String extractText(GeminiResponse response) {
    return java.util.Optional.ofNullable(response)
        .map(GeminiResponse::candidates)
        .filter(c -> !c.isEmpty())
        .map(c -> c.get(0))
        .map(GeminiResponse.Candidate::content)
        .map(GeminiResponse.Content::parts)
        .filter(p -> !p.isEmpty())
        .map(p -> p.get(0))
        .map(GeminiResponse.Part::text)
        .filter(text -> !text.isBlank()) // 確保文字不是只有空白
        .orElseThrow(() -> new RuntimeException("Gemini API 回傳內容解析失敗或內容為空"));
  }

  private record GeminiRequest(
      @JsonProperty("system_instruction") SystemInstruction systemInstruction,
      List<Content> contents) {
    record SystemInstruction(List<Part> parts) {}

    record Content(String role, List<Part> parts) {}

    record Part(String text) {}
  }

  private record GeminiResponse(List<Candidate> candidates) {
    record Candidate(Content content) {}

    record Content(List<Part> parts, String role) {}

    record Part(String text) {}
  }
}
