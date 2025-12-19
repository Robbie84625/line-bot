package com.robbie.linebot.infra.provider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

  public String chat(String message) throws Exception {

    String url = UriComponentsBuilder.fromUriString(apiUrl).queryParam("key", apiKey).toUriString();

    JsonObject requestBody = new JsonObject();

    // 建構 system instruction
    JsonObject systemInstruction = new JsonObject();
    JsonObject systemParts = new JsonObject();
    systemParts.addProperty("text", systemPrompt);
    systemInstruction.add("parts", gson.toJsonTree(new JsonObject[] {systemParts}));
    requestBody.add("system_instruction", systemInstruction);

    JsonObject content = new JsonObject();
    JsonObject parts = new JsonObject();
    parts.addProperty("text", message);
    content.add("parts", gson.toJsonTree(new JsonObject[] {parts}));
    requestBody.add("contents", gson.toJsonTree(new JsonObject[] {content}));

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // 檢查 HTTP 狀態碼
    if (response.statusCode() != 200) {
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
}
