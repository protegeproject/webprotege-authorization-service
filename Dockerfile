FROM openjdk:17
MAINTAINER protege.stanford.edu

EXPOSE 7771
ARG JAR_FILE
COPY target/${JAR_FILE} webprotege-authorization-service.jar
ENTRYPOINT ["java","--add-opens=java.management/sun.net=ALL-UNNAMED","-jar","/webprotege-authorization-service.jar"]