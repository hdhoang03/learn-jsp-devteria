
FROM eclipse-temurin:17-jdk


ENV SPRING_PROFILES_ACTIVE=prod


WORKDIR /app


COPY target/*.jar app.jar


EXPOSE 8080


CMD ["java", "-jar", "app.jar"]
