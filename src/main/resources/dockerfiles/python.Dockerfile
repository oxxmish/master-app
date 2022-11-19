FROM ${runnerImage}
WORKDIR ${workdir}
COPY . ${workdir}
RUN pip install -r requirements.txt
ENTRYPOINT python ${executableName}
