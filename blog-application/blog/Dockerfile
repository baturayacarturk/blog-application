FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

COPY src ./src

COPY src/main/resources/application.yml ./
COPY src/main/resources/index-creation-script.json /app/index-creation-script.json
RUN ./gradlew build -x test

EXPOSE 8080

CMD ["java", "-jar", "/app/build/libs/blog-0.0.1-SNAPSHOT.jar"]
