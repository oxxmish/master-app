package ru.freemiumhosting.master.properties;

import lombok.Data;

@Data
public class DockerBuildParams {
    private String builderImage;
    private String buildCommand;
    private String javaImage;
}
