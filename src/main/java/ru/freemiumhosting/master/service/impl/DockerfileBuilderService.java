package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class DockerfileBuilderService {
    //TODO: extract following to configuration-properties
    @Value("${freemium.hosting.default-builder-path}")
    private String DOCKERFILE_PATH;
    @Value("${freemium.hosting.dockerfile.builder-image}")
    private String BUILDER_IMAGE;
    @Value("${freemium.hosting.dockerfile.workdir}")
    private String WORKDIR;
    @Value("${freemium.hosting.dockerfile.build-command}")
    private String BUILD_COMMAND;
    @Value("${freemium.hosting.dockerfile.java-image}")
    private String JAVA_IMAGE;

    @SneakyThrows
    public void createJavaDockerFile(String jarName, String runArgs) {
        String dockerfileString = generateJavaDockerFileString(jarName, runArgs);
        Path path = Paths.get(DOCKERFILE_PATH);
        Files.write(path, dockerfileString.getBytes(StandardCharsets.UTF_8));
    }

    String generateJavaDockerFileString(String jarName, String runArgs) {
        StringBuilder builder = new StringBuilder();
        builder.append("FROM ").append(BUILDER_IMAGE).append(" as builder\n")
                .append("COPY . ").append(WORKDIR).append("\n")
                .append("WORKDIR ").append(WORKDIR).append("\n")
                .append("RUN ").append(BUILD_COMMAND).append("\n")
                .append("FROM ").append(JAVA_IMAGE).append("\n")
                .append("WORKDIR ").append(WORKDIR).append("\n")
                //TODO убрать захардкоженный target
                .append("COPY --from=builder ").append(WORKDIR).append("/target/").append(jarName).append("\n")
                .append("ENTRYPOINT java -jar ").append(jarName).append(" ").append(runArgs);
        return builder.toString();
    }
}
