package ru.freemiumhosting.master.service.builderinfo;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.maven.PomXmlStructure;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;

import java.io.File;

@Service
@RequiredArgsConstructor
public class MavenInfoService implements BuilderInfoService {
    private final XmlMapper xmlMapper;

    @Override
    @SneakyThrows
    public String getExecutableFileName(String pathToProject) {
        PomXmlStructure pomXmlStructure = xmlMapper.readValue(new File(pathToProject + "\\pom.xml"), PomXmlStructure.class);
        return pomXmlStructure.groupId + pomXmlStructure.artifactId + pomXmlStructure.version + ".jar"; //TODO: имя выходного файла может быть переопределено средствами плагина, лучше просто искать jarник в target
    }

    @Override
    public String supportedLanguage() {
        return "java";
    }
}
