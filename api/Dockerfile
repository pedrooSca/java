FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM tomcat:9.0-jdk25-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/api.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
