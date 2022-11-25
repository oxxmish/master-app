package ru.freemiumhosting.master.service.builderinfo;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class DockerInfoService implements BuilderInfoService {
    public static final String DOCKER_LANG = "Docker";

    @Override
    public String validateProjectAndGetExecutableFileName(String pathToProject) {
        if (!Path.of(pathToProject, "Dockerfile").toFile().exists()) {
            throw new IllegalArgumentException(
                "Docker was specified as target platform, but no Dockerfile in project root was found");
        }
        return null;
    }

    @Override
    public String supportedLanguage() {
        return DOCKER_LANG;
    }
}
