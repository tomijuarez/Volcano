FROM openjdk:10-jre-slim
COPY ./target/volcano-0.0.1-SNAPSHOT.jar /usr/src/volcano/
WORKDIR /usr/src/volcano
EXPOSE 8080
CMD ["java", "-jar", "volcano-0.0.1-SNAPSHOT.jar"]