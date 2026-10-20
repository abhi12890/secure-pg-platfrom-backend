# Multi-stage Spring Boot Dockerfile — non-root runtime
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache wget \
    && addgroup -S springgroup && adduser -S springuser -G springgroup
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R springuser:springgroup /app
USER springuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
