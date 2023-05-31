FROM openjdk:11.0.16-jre

ARG KUBECONFIG_PATH=./config.yaml
ARG JAR_FILE=app.jar
ARG JAR_OPTS=""
ARG JAVA_OPTS=""
ARG APP_NAME=app

VOLUME /opt/app/db

ENV JAVA_DEFAULT_OPTS "-XX:+UnlockExperimentalVMOptions \
    -Djava.net.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=0 \
    -Dnetworkaddress.cache.negative.ttl=0 \
    -Xnoagent \
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
COPY ${KUBECONFIG_PATH} /opt/app/config.yaml

RUN chmod -w /opt/app/${APP_NAME}.jar && chmod -w /opt/app/config.yaml

ENV KUBECONFIG_PATH /opt/app/config.yaml

#TODO: remove this, this is to test that kaniko works
#ENTRYPOINT echo "FROM nginx" > /opt/Dockerfile && mkdir -p /kaniko/.docker && \
#           echo "{\"auths\":{\"$REGISTRY_URL\":{\"username\":\"$REGISTRY_LOGIN\",\"password\":\"$REGISTRY_PASSWORD\"}}}" > /kaniko/.docker/config.json && \
#           /kaniko/executor  --context /opt/ \
#                 --dockerfile /opt/Dockerfile \
#                 --destination $REGISTRY_URL/freemiumhosting/test-app2:latest


CMD exec java \
    ${JAVA_DEFAULT_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    ${JAVA_OPTS} \
    -jar /opt/app/${APP_NAME}.jar \
    ${JAR_OPTS}
