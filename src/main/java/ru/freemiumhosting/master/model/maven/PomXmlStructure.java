package ru.freemiumhosting.master.model.maven;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "project")
public class PomXmlStructure {
    @JacksonXmlProperty(localName = "groupId")
    public String groupId;

    @JacksonXmlProperty(localName = "artifactId")
    public String artifactId;

    @JacksonXmlProperty(localName = "version")
    public String version;
}
