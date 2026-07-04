FROM eclipse-temurin:21-jre-alpine

USER root
RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --chown=spring:spring ${JAR_FILE} app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]