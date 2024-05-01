FROM openjdk:21

COPY core-1.0.0-SNAPSHOT.jar core.jar
COPY /configurations/ /configurations/
COPY /modules/ /modules/
COPY /locale/ /locale/

ENTRYPOINT ["java","-jar","core.jar"]