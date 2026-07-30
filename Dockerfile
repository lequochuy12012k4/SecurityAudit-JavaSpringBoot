FROM openjdk:26-ea

WORKDIR /app

COPY ./target/spring-boot-app-0.0.1-SNAPSHOT.jar /app/
COPY ./src/main/resources/application.yaml /app/application.yaml 

CMD ["java", "-jar", "spring-boot-app-0.0.1-SNAPSHOT.jar"]