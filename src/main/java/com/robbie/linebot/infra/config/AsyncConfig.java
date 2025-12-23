package com.robbie.linebot.infra.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean(name = "geminiExecutor")
  public Executor geminiExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // 平時有 2 個 Worker 在等
    executor.setCorePoolSize(2);
    // 最多 5 個，防止把 API 配額噴光
    executor.setMaxPoolSize(5);
    // 隊列長度 100
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("GeminiWorker-");
    executor.initialize();
    return executor;
  }
}
