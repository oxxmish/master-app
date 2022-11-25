package ru.freemiumhosting.master.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.properties.DockerfilesProperties;

@Service
@RequiredArgsConstructor
public class DockerfileBuilderService {
    private final DockerfilesProperties dockerfilesProperties;
    private final String WORKDIR_PARAM = "workdir";
    private final String EXECUTABLE_PARAM = "executableName";


    @Value("${freemium.hosting.default-builder-path}")
    private String DOCKERFILE_PATH;

    @SneakyThrows
    public void createDockerFile(String language, String executableFileName, String runArgs, Long projectId) {
        var dockerBuildParams = dockerfilesProperties.getImageParams().get(language);
        String dockerfileString = generateDockerFileString(language, dockerBuildParams, executableFileName);
        Path path = Paths.get(DOCKERFILE_PATH, String.valueOf(projectId), "Dockerfile");
        Files.write(path, dockerfileString.getBytes(StandardCharsets.UTF_8));
    }

    String generateDockerFileString(String language, Map<String, String> dockerBuildParams,
                                    String executableName) {
        var fullDockerParams = new HashMap<>(dockerBuildParams);
        fullDockerParams.put(WORKDIR_PARAM, dockerfilesProperties.getWorkdir());
        fullDockerParams.put(EXECUTABLE_PARAM, executableName);
        var dockerFileTemplate = getDockerFileTemplate(language);
        var substitutor = new StringSubstitutor(fullDockerParams);
        return substitutor.replace(dockerFileTemplate);
    }

    @SneakyThrows
    private static String getDockerFileTemplate(String language) {
        return new String(DockerfilesProperties.class.getResourceAsStream(MessageFormat.format(
            "/dockerfiles/{0}.Dockerfile", language)).readAllBytes()); //TODO: cache
    }
}
