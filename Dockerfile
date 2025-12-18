# 建構階段
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
# 給予執行權限並執行
RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon

# 執行階段
FROM openjdk:17-jdk-slim
WORKDIR /app

# 只複製正確的 jar
COPY --from=build /app/build/libs/*[!plain].jar app.jar

ENV TZ=Asia/Taipei
# 讓 Java 自動偵測 Cloud Run 給予的 PORT 環境變數
ENV PORT=8080
EXPOSE 8080

# 增加 -Djava.security.egd 以加快啟動速度（容器環境常用）
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Xmx512m", "-jar", "app.jar"]