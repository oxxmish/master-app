package ru.freemiumhosting.master.service.impl;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
class DockerfileBuilderServiceTest {
    @Autowired
    private DockerfileBuilderService dockerfileBuilderService;

    private static final String expectedJavaDockerfile =
        "FROM maven:3.6.3-jdk-14 as builder\n" +
            "COPY . /usr/src/app/backend\n" +
            "WORKDIR /usr/src/app/backend\n" +
            "RUN mvn install -DskipTests=true\n" +
            "FROM openjdk:14-jdk\n" +
            "WORKDIR /usr/src/app/backend\n" +
            "COPY --from=builder /usr/src/app/backend/target/app.jar\n" +
            "ENTRYPOINT java -jar app.jar ";

    @Test
    void testGenerateJavaDockerfile() {
        assertEquals(expectedJavaDockerfile, dockerfileBuilderService.generateJavaDockerFileString("app.jar", ""));
    }
}
