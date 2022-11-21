ARG BASE_IMAGE=gcr.io/kaniko-project/executor:v1.9.0-debug

FROM ${BASE_IMAGE}

ENV JDK_TAR_PATH="./jdk11.tar.gz"
SHELL  ["/busybox/sh", "-c"]

COPY ${JDK_TAR_PATH} ./
RUN ls
RUN tar -xzf jdk11.tar.gz
RUN rm jdk11.tar.gz
RUN ls


ENV JAVA_HOME "/workspace/jdk-11.0.17+8/bin"
ENV PATH $PATH:$JAVA_HOME
RUN chmod +x $JAVA_HOME/java


ARG JAR_FILE=app.jar
ARG JAR_OPTS=""
ARG JAVA_OPTS=""
ARG APP_NAME=app
ARG REGISTRY_LOGIN
ARG REGISTRY_PASSWORD

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

WORKDIR /opt/app/
COPY ${JAR_FILE} /opt/app/${APP_NAME}.jar
RUN echo $JAVA_HOME
RUN ls $JAVA_HOME


ENTRYPOINT exec $JAVA_HOME/java \
    ${JAVA_DEFAULT_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    ${JAVA_OPTS} \
    -jar /opt/app/${APP_NAME}.jar \
    ${JAR_OPTS}