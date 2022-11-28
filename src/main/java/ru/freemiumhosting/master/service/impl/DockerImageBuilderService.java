package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.exception.KanikoException;
import ru.freemiumhosting.master.model.Project;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerImageBuilderService {

    @Value("${freemium.hosting.git-clone-path}")
    private String pathToProjects;
    @Value("${freemium.hosting.registry.url}")
    private String registryUrl;
    @Value("${freemium.hosting.registry.default-repo}")
    private String repository;

    @SneakyThrows
    public void pushImageToRegistry(Project project) {
        log.info("Старт загрузки образа в registry");
        String destination = repository + "/" + project.getName() + ":" + project.getBranch();
        String kanikoDestination = registryUrl + "/" + destination;
        String dockerPath = pathToProjects + "/" + project.getName() + "/Dockerfile";
        String context = "/opt";
        Process process = new ProcessBuilder()
                .command(String.format("/kaniko/executor --context=%s --dockerfile=%s --destination=%s", context, dockerPath, kanikoDestination))
                .inheritIO()
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new KanikoException("Ошибка при загрузке образа в dockerHub");
        }
        project.setRegistryDestination(destination);
    }

}
