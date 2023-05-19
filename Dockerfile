FROM openjdk:11

ARG JAVA_OPTS

ENV JAVA_OPTS=$JAVA_OPTS

COPY build/libs/meme-generator-be-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

RUN sh -c 'mkdir /images'

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar