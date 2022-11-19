package ru.freemiumhosting.master.service.builderinfo;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PythonInfoService implements BuilderInfoService {
    private static final String REQUIREMENTS_FILE = "requirements.txt";

    @Value("${freemium.hosting.dockerfile.imageParams.python.appName}")
    private String appName;

    @Override
    public String validateProjectAndGetExecutableFileName(String pathToProject) throws InvalidProjectException {
        var appPyPath = Path.of(pathToProject, appName);
        if (!Files.exists(appPyPath)) {
            throw new InvalidProjectException("Проект не содержит исполняемый файл app.py");
        } else if (!Files.exists(Path.of(pathToProject, REQUIREMENTS_FILE))) {
            throw new InvalidProjectException("Проект не содержит файл requirements.txt");
        }
        return appName;
    }

    @Override
    public String supportedLanguage() {
        return "python";
    }
}
