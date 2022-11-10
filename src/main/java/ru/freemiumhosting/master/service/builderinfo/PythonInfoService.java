package ru.freemiumhosting.master.service.builderinfo;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PythonInfoService implements BuilderInfoService {
    @Value("freemium.hosting.dockerfile.imageParams.python.appName")
    private String appName;

    @Override
    public String getExecutableFileName(String pathToProject) {
        return Path.of(pathToProject, appName).toString();
    }

    @Override
    public String supportedLanguage() {
        return "python";
    }
}
