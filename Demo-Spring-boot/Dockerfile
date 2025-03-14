
FROM maven:3.9.9 AS build



WORKDIR /app


COPY . .


RUN mvn clean package -DskipTests


FROM openjdk:17
WORKDIR /app


COPY --from=build /app/target/*.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]
