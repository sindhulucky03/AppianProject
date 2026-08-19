FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
RUN addgroup --system audit && adduser --system --ingroup audit audit
WORKDIR /app
COPY --from=build /workspace/target/audit-log-service-0.0.1-SNAPSHOT.jar app.jar
USER audit:audit
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
