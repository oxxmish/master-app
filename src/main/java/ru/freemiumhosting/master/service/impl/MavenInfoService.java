package ru.freemiumhosting.master.service.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.maven.PomXmlStructure;
import ru.freemiumhosting.master.service.BuilderInfoService;

import java.io.File;

@Service
@RequiredArgsConstructor
public class MavenInfoService implements BuilderInfoService {
    private final XmlMapper xmlMapper;

    @Override
    @SneakyThrows
    public String getJarFileName(String pathToPom) {
        PomXmlStructure pomXmlStructure = xmlMapper.readValue(new File(pathToPom + "\\pom.xml"), PomXmlStructure.class);
        return pomXmlStructure.groupId + pomXmlStructure.artifactId + pomXmlStructure.version + ".jar"; //TODO: имя выходного файла может быть переопределено средствами плагина, лучше просто искать jarник в target
    }
}
