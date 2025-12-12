package com.robbie.linebot.infra.provider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiAIProvider {
  @Value("${gemini.api.key}")
  private String apiKey;
  @Value("${gemini.api.url}")
  private String apiUrl;

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final Gson gson = new Gson();

  public String chat(String message) {
    try {
      String url = apiUrl+ apiKey;

      JsonObject requestBody = new JsonObject();
      JsonObject content = new JsonObject();
      JsonObject parts = new JsonObject();
      parts.addProperty("text", message);
      content.add("parts", gson.toJsonTree(new JsonObject[]{parts}));
      requestBody.add("contents", gson.toJsonTree(new JsonObject[]{content}));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
      return jsonResponse.getAsJsonArray("candidates")
          .get(0).getAsJsonObject()
          .getAsJsonObject("content")
          .getAsJsonArray("parts")
          .get(0).getAsJsonObject()
          .get("text").getAsString();

    } catch (Exception e) {
      return "抱歉,發生錯誤: " + e.getMessage();
    }
  }
}
