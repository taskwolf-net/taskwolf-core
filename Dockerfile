FROM alpine

COPY /build/libs/core-1.0.0-SNAPSHOT.jar core.jar
COPY /locale/ /locale/
COPY /geo/ /geo/