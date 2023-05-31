package ru.freemiumhosting.master.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.service.impl.DockerImageBuilderService;
import ru.freemiumhosting.master.service.impl.DockerfileBuilderService;
import ru.freemiumhosting.master.service.impl.GitService;
import ru.freemiumhosting.master.service.impl.KubernetesService;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.utils.exception.GitCloneException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployService {
    private final DockerImageBuilderService dockerImageBuilderService;
    private final DockerfileBuilderService dockerfileBuilderService;
    private final GitService gitService;
    private final CleanerService cleanerService;
    private final ProjectRep projectRep;
    private final KubernetesService kubernetesService;

    @Async
    @SneakyThrows
    public void deployProject(Project project) throws DeployException {
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        project = projectRep.save(project);

        project = dockerImageBuilderService.buildProject(project);

        createKubernetesObj(project);

        project.setStatus(ProjectStatus.ACTIVE);
        projectRep.save(project);
    }

    public void createKubernetesObj(Project project) {
        project.setKubernetesName(String.format("%s-%s", project.getOwnerName(), project.getId()));
        kubernetesService.createNamespaceIfDontExist(project);
        kubernetesService.createOrReplaceService(project);
        kubernetesService.createOrReplaceDeployment(project);
    }

    @Async
    @SneakyThrows
    public void redeployProject(Project project) throws DeployException {
        //TODO delete kube objects if exist
        deployProject(project);
    }

}
