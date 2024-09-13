FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

COPY src ./src

COPY src/main/resources/application.yml ./

RUN apt-get update && apt-get install -y curl

RUN ./gradlew build -x test

EXPOSE 8070

CMD ["java", "-jar", "/app/build/libs/eurekaserver-0.0.1-SNAPSHOT.jar"]