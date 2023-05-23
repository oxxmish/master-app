package ru.freemiumhosting.master.service.impl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.utils.properties.DockerfilesProperties;

@Service
@RequiredArgsConstructor
public class DockerfileBuilderService {
    private final DockerfilesProperties dockerfilesProperties;
    private final String WORKDIR_PARAM = "workdir";
    private final String EXECUTABLE_PARAM = "executableName";



    @SneakyThrows
    public void createDockerFile(Path dockerfilePath, String language, String executableName, String runArgs) {
        var dockerBuildParams = dockerfilesProperties.getImageParams().get(language);
        String dockerfileString = generateDockerFileString(language, dockerBuildParams, executableName);
        Files.write(dockerfilePath, dockerfileString.getBytes(StandardCharsets.UTF_8));
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
