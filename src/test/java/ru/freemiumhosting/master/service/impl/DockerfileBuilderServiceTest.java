package ru.freemiumhosting.master.service.impl;

import static org.junit.jupiter.api.Assertions.*;


import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.utils.properties.DockerfilesProperties;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
@Disabled
class DockerfileBuilderServiceTest {
    @Autowired
    private DockerfileBuilderService dockerfileBuilderService;

    @Autowired
    private DockerfilesProperties dockerfilesProperties;

    private static final String expectedPythonDockerfile =
        "FROM python:3.9\n" +
            "WORKDIR /usr/src/app/backend\n" +
            "COPY . /usr/src/app/backend\n" +
            "RUN pip install -r requirements.txt\n" +
            "ENTRYPOINT python /usr/src/app/backend/app.py\n";

    private static final String expectedJavaDockerfile =
        "FROM maven:3.6.3-jdk-11 as builder\n" +
            "COPY . /usr/src/app/backend\n" +
            "WORKDIR /usr/src/app/backend\n" +
            "RUN mvn install -DskipTests=true\n" +
            "FROM openjdk:11.0.16-jre\n" +
            "WORKDIR /usr/src/app/backend\n" +
            "COPY --from=builder /usr/src/app/backend/target/app.jar app.jar\n" +
            "ENTRYPOINT java -jar app.jar\n";

    public static Stream<Arguments> getCases() {
        return Stream.of(
            Arguments.of("java", "app.jar", expectedJavaDockerfile),
            Arguments.of("python", "app.py", expectedPythonDockerfile)
        );
    }

    @MethodSource("getCases")
    @ParameterizedTest
    void testGenerateDockerfile(String language, String executable, String result) {
        assertEquals(result.trim(), dockerfileBuilderService.generateDockerFileString(
            language, dockerfilesProperties.getImageParams().get(language), executable).trim());
    }
}
