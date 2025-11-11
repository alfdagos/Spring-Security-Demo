FROM eclipse-temurin:21-jdk-jammy

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java","-jar","/app.jar"]
