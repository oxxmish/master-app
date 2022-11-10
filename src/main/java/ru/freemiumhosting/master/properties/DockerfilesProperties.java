package ru.freemiumhosting.master.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "freemium.hosting.dockerfile")
public class DockerfilesProperties {
    private String workdir;
    private Map<String, Map<String, String>> imageParams;
}
