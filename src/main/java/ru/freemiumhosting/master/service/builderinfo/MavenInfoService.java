package ru.freemiumhosting.master.service.builderinfo;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.exception.InvalidProjectException;
import ru.freemiumhosting.master.model.maven.PomXmlStructure;

import java.io.File;

@Service
@RequiredArgsConstructor
public class MavenInfoService implements BuilderInfoService {
    private final XmlMapper xmlMapper;

    @Override
    @SneakyThrows
    public String validateProjectAndGetExecutableFileName(String pathToProject) {
        var pomFile = Path.of(pathToProject, "pom.xml").toFile();
        if (!pomFile.exists()) {
            throw new InvalidProjectException("Проект не содержит исполняемый файл pom.xml");
        }
        PomXmlStructure pomXmlStructure = xmlMapper.readValue(pomFile, PomXmlStructure.class);
        return pomXmlStructure.artifactId + "-" + pomXmlStructure.version + ".jar"; //TODO: имя выходного файла может быть переопределено средствами плагина, лучше просто искать jarник в target
    }

    @Override
    public String supportedLanguage() {
        return "java";
    }
}
