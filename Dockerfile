FROM openjdk:16-alpine
MAINTAINER protege.stanford.edu

EXPOSE 7771

COPY target/webprotege-authorization-service-0.1.0.jar webprotege-authorization-service-0.1.0.jar
ENTRYPOINT ["java","-jar","/webprotege-authorization-service-0.1.0.jar"]