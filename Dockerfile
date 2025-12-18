# --- 前半段保持不變 ---
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon

# --- 修改這裡：更換基礎鏡像 ---
FROM eclipse-temurin:17-jdk-alpine
# 或者用 eclipse-temurin:17-jre-focal (如果 alpine 跑不起來)

WORKDIR /app

# 從建構階段複製 JAR 檔案
# 加上 [!plain] 是為了避免複製到兩個 JAR 導致啟動錯誤
COPY --from=build /app/build/libs/*[!plain].jar app.jar

ENV TZ=Asia/Taipei
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]