ARG BASE_IMAGE=openjdk:11.0.16-jre

FROM ${BASE_IMAGE}

ARG JAR_FILE=app.jar
ARG JAR_OPTS=""
ARG JAVA_OPTS=""
ARG APP_NAME=app

VOLUME /opt/app/db

ENV JAVA_DEFAULT_OPTS "-XX:+UnlockExperimentalVMOptions \
    -Djava.net.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=0 \
    -Dnetworkaddress.cache.negative.ttl=0 \
    -Xdebug -Xnoagent \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Duser.country=US -Duser.language=en \
    -Duser.timezone=UTC -Dfile.encoding=UTF-8 \
    -Djava.security.egd=file:/dev/./urandom"

# Additional JAVA options
ENV JAVA_OPTS "${JAVA_OPTS}"
# Additional JAR options
ENV JAR_OPTS "${JAR_OPTS}"

ENV ENV "default"
ENV APP_NAME "$APP_NAME"
ENV SPRING_PROFILES_ACTIVE "prod"
ENV SERVER_PORT 8087

WORKDIR /opt/app/
COPY ${JAR_FILE} /opt/app/${APP_NAME}.jar

CMD exec java \
    ${JAVA_DEFAULT_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    ${JAVA_OPTS} \
    -jar /opt/app/${APP_NAME}.jar \
    ${JAR_OPTS}