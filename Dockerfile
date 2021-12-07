FROM openjdk:16-alpine
MAINTAINER protege.stanford.edu

EXPOSE 7771

COPY target/*.jar webprotege-authorization-service.jar
ENTRYPOINT ["java","-jar","/webprotege-authorization-service.jar"]