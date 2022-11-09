package ru.freemiumhosting.master.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.properties.DockerBuildParams;
import ru.freemiumhosting.master.properties.DockerfilesProperties;

@Service
@RequiredArgsConstructor
public class DockerfileBuilderService {
    private final DockerfilesProperties dockerfilesProperties;

    @Value("${freemium.hosting.default-builder-path}")
    private String DOCKERFILE_PATH;

    @SneakyThrows
    public void createDockerFile(String language, String jarName, String runArgs) {
        var dockerBuildParams = dockerfilesProperties.getImages().get(language);
        String dockerfileString = generateDockerFileString(dockerBuildParams, jarName, runArgs);
        Path path = Paths.get(DOCKERFILE_PATH);
        Files.write(path, dockerfileString.getBytes(StandardCharsets.UTF_8));
    }

    private String generateDockerFileString(DockerBuildParams dockerBuildParams, String executableName,
                                            String runArgs) {
        StringBuilder builder = new StringBuilder();
        addBuilderStageIfNeed(dockerBuildParams, builder);
        builder
            .append("FROM ").append(dockerBuildParams.getRunnerImage()).append("\n")
            .append("WORKDIR ").append(dockerfilesProperties.getWorkdir()).append("\n");
        //TODO убрать захардкоженный target
        addCopy(dockerBuildParams, executableName, builder);
        builder.append("ENTRYPOINT ").append("java -jar ").append(executableName).append(" ").append(runArgs);
        return builder.toString();
    }

    private void addCopy(DockerBuildParams dockerBuildParams, String jarName,
                         StringBuilder builder) {
        if (dockerBuildParams.hasBuilderImage()) {
            builder.append("COPY --from=builder ").append(dockerfilesProperties.getWorkdir())
                .append("/target/").append(jarName).append("\n");
        } else {
            builder.append("COPY . ").append(dockerfilesProperties.getWorkdir()).append("\n");
        }
    }

    private void addBuilderStageIfNeed(DockerBuildParams dockerBuildParams, StringBuilder builder) {
        if (dockerBuildParams.hasBuilderImage()) {
            builder.append("FROM ").append(dockerBuildParams.getBuilderImage())
                .append(" as builder\n")
                .append("COPY . ").append(dockerfilesProperties.getWorkdir()).append("\n")
                .append("WORKDIR ").append(dockerfilesProperties.getWorkdir()).append("\n")
                .append("RUN ").append(dockerBuildParams.getBuildCommand()).append("\n");
        }
    }
}
