FROM java:14
FROM maven:alpine
WORKDIR /usr/src/myapp
ADD pom.xml /usr/src/myapp
RUN mvn -v
RUN mvn install
ADD target/audit-box-0.2.0-SNAPSHOT.jar /package/
ENTRYPOINT ["java","-jar","/package/audit-box-0.2.0-SNAPSHOT.jar"]
