FROM maven:3-jdk-11 as build

RUN mkdir /src
WORKDIR /src
COPY . /src
RUN mvn package

FROM openjdk:11-slim

RUN mkdir /app
COPY --from=build /src/target/pasta-backend-*.jar /app/pasta-backend.jar
WORKDIR /app

CMD java -jar pasta-backend.jar
