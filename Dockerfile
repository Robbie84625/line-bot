# 使用 Gradle 建構階段
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# 複製 Gradle 配置檔案
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 複製原始碼
COPY src ./src

# 建構應用程式
RUN gradle clean build -x test --no-daemon

# 執行階段
FROM openjdk:17-jdk-slim

WORKDIR /app

# 從建構階段複製 JAR 檔案
COPY --from=build /app/build/libs/*.jar app.jar

# 設定時區（可選）
ENV TZ=Asia/Taipei

# 暴露 8080 port
EXPOSE 8080

# 啟動應用程式
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]