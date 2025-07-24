# 第一阶段：构建 jar 包
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：只保留 jar 包，减小镜像体积
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/game9-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"] 