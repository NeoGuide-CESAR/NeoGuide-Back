# Estágio 1: Build da aplicação com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Estágio 2: Imagem leve de execução (JRE 21) com hardening não-root
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup /app/app.jar

USER appuser

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -XX:+UseStringDeduplication -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=25 -XX:G1PeriodicGCInterval=15000 -XX:ReservedCodeCacheSize=48m -Xss256k"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
