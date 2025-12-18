dockerfileFROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*[!plain].jar app.jar

ENV TZ=Asia/Taipei
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-Dserver.port=${PORT}", "-jar", "app.jar"]