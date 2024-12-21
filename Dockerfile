FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
RUN mvn -f /app/pom.xml dependency:go-offline
COPY src /app/src
RUN mvn -f /app/pom.xml package -DskipTests

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/**.jar app.jar
CMD ["java", "-jar", "app.jar"]
