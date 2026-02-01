FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine AS ganok-bot
WORKDIR /app

COPY logback.xml ./
COPY config ./config

COPY --from=builder /build/target/GanokBot.jar ./GanokBot.jar

EXPOSE 8080
EXPOSE 5005

ENTRYPOINT [ \
    "java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-Dspring.config.location=./config/application.properties", \
    "-jar", \
    "-Xdebug", \
    "GanokBot.jar" \
]