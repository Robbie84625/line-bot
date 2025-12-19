package com.robbie.linebot.infra.provider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class GeminiAIProvider {
  @Value("${gemini.api.key}")
  private String apiKey;

  @Value("${gemini.api.url}")
  private String apiUrl;

  @Value("${gemini.system.prompt}")
  private String systemPrompt;

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final Gson gson = new Gson();

  private final AtomicLong lastRequestTime = new AtomicLong(0);
  private static final long MIN_REQUEST_INTERVAL = 4000;

  public String chat(String message) throws Exception {

    rateLimit();

    String url = UriComponentsBuilder.fromUri(URI.create(apiUrl)).queryParam("key", apiKey).toUriString();

    JsonObject requestBody = new JsonObject();

    // 建構 system instruction
    JsonArray systemParts = new JsonArray();
    JsonObject systemPart = new JsonObject();
    systemPart.addProperty("text", systemPrompt);
    systemParts.add(systemPart);

    JsonObject systemInstruction = new JsonObject();
    systemInstruction.add("parts", systemParts);
    requestBody.add("system_instruction", systemInstruction);

    JsonArray contentParts = new JsonArray();
    JsonObject contentPart = new JsonObject();
    contentPart.addProperty("text", message);
    contentParts.add(contentPart);

    JsonObject content = new JsonObject();
    content.add("parts", contentParts);

    JsonArray contents = new JsonArray();
    contents.add(content);
    requestBody.add("contents", contents);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // 檢查 HTTP 狀態碼
    if (response.statusCode() == 429) {
      log.error("Gemini API 限流錯誤 (429)");
      throw new RuntimeException("RATE_LIMIT_429");
    }

    if (response.statusCode() == 404) {
      log.error("Gemini API 路徑錯誤 (404): {}", response.body());
      throw new RuntimeException("API_NOT_FOUND_404");
    }

    if (response.statusCode() != 200) {
      log.error("Gemini API 回應錯誤: {} - {}", response.statusCode(), response.body());
      throw new RuntimeException("Gemini API 回應錯誤: " + response.statusCode());
    }

    JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
    return jsonResponse
        .getAsJsonArray("candidates")
        .get(0)
        .getAsJsonObject()
        .getAsJsonObject("content")
        .getAsJsonArray("parts")
        .get(0)
        .getAsJsonObject()
        .get("text")
        .getAsString();
  }

  private void rateLimit() {
    synchronized (this) {
      long now = System.currentTimeMillis();
      long timeSinceLastRequest = now - lastRequestTime.get();

      if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
        long waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
        log.info("限流中,等待 {} ms", waitTime);
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("限流等待被中斷", e);
        }
      }

      lastRequestTime.set(System.currentTimeMillis());
    }
  }
}
