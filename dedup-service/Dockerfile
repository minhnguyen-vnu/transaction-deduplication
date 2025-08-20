FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/dedup-service.jar dedup-service.jar

EXPOSE 8080

CMD ["java","-jar","dedup-service.jar"]