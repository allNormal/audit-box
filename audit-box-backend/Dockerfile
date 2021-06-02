FROM maven:3.5.2-jdk-8 AS build
WORKDIR /usr/src/myapp
COPY pom.xml /usr/src/myapp
COPY src /usr/src/myapp/src
RUN mvn -f /usr/src/myapp/pom.xml clean install


FROM openjdk:8-jdk-alpine
COPY --from=build /usr/src/myapp/target/audit-box-0.2.0-SNAPSHOT-spring-boot.jar /usr/myapp/audit-box-0.2.0-SNAPSHOT-spring-boot.jar
EXPOSE 2806
RUN mkdir /target
ENTRYPOINT ["java","-jar","/usr/myapp/audit-box-0.2.0-SNAPSHOT-spring-boot.jar"]

