package ru.freemiumhosting.master.properties;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class DockerBuildParams {
    private String builderImage;
    private String buildCommand;
    private String runnerImage;

    public boolean hasBuilderImage() {
        return !StringUtils.isEmpty(builderImage);
    }
}
