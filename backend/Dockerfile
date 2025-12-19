FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY backend/src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN mkdir -p /app/uploads
COPY --from=build /app/target/*.jar app.jar
COPY backend/src/main/resources/uploads/ /app/uploads/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
