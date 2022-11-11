FROM ${builderImage} as builder
COPY . ${workdir}
WORKDIR ${workdir}
RUN ${buildCommand}
FROM ${runnerImage}
WORKDIR ${workdir}
COPY --from=builder ${workdir}/target/${executableName}
ENTRYPOINT java -jar ${executableName}
