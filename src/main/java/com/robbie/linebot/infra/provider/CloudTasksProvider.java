package com.robbie.linebot.infra.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.OidcToken;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;
import com.robbie.linebot.model.ChatTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CloudTasksProvider {
  @Value("${gcp.project-id}")
  private String projectId;

  @Value("${gcp.location}")
  private String location;

  @Value("${gcp.queue-id}")
  private String queueId;

  @Value("${gcp.backend-url}")
  private String backendUrl;

  @Value("${gcp.service-account-email}")
  private String serviceAccountEmail;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void enqueueTask(ChatTask task) {
    try (CloudTasksClient client = CloudTasksClient.create()) {
      String queuePath = QueueName.of(projectId, location, queueId).toString();

      // 將 Command 轉為 JSON
      String payload = objectMapper.writeValueAsString(task);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .setUrl(backendUrl + "/internal/tasks/process-ai")
              .setHttpMethod(HttpMethod.POST)
              .putHeaders("Content-Type", "application/json")
              .setBody(ByteString.copyFromUtf8(payload))
              .setOidcToken(OidcToken.newBuilder()
                  .setServiceAccountEmail(serviceAccountEmail)
                  .build())
              .build();

      Task gcpTask = Task.newBuilder().setHttpRequest(httpRequest).build();

      client.createTask(queuePath, gcpTask);
      log.info("[Cloud Tasks] 任務已成功排隊: {}", task.getMessageId());
    } catch (Exception e) {
      log.error("[Cloud Tasks] 任務排隊失敗", e);
      throw new RuntimeException(e);
    }
  }
}
