package ru.freemiumhosting.master.service.impl;

import static ru.freemiumhosting.master.service.builderinfo.DockerInfoService.DOCKER_LANG;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.exception.DeployException;
import ru.freemiumhosting.master.exception.KuberException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;
import ru.freemiumhosting.master.service.ProjectService;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final String clonePath;
    private final GitService gitService;
    private final KubernetesService kubernetesService;
    private final DockerfileBuilderService dockerfileBuilderService;
    // key - language, value - BuilderInfoService
    private final Map<String, BuilderInfoService> builderInfoServices;
    private final DockerImageBuilderService dockerImageBuilderService;
    private final ProjectRep projectRep;


    public ProjectServiceImpl(@Value("${freemium.hosting.git-clone-path}") String clonePath,
                              GitService gitService,
                              KubernetesService kubernetesService, DockerfileBuilderService dockerfileBuilderService,
                              Collection<BuilderInfoService> builderInfoServices,
                              DockerImageBuilderService dockerImageBuilderService,
                              ProjectRep projectRep) {
        this.clonePath = clonePath;
        this.gitService = gitService;
        this.kubernetesService = kubernetesService;
        this.dockerfileBuilderService = dockerfileBuilderService;
        this.dockerImageBuilderService = dockerImageBuilderService;
        this.builderInfoServices = builderInfoServices.stream().collect(Collectors.toMap(builderInfoService ->
                builderInfoService.supportedLanguage().toLowerCase(Locale.ROOT), s -> s));
        this.projectRep = projectRep;
    }

    @Override
    public void createProject(Project project) throws DeployException {
        deployProject(project);
        projectRep.save(project);
    }

    @Override
    public void deployProject(Project project) throws DeployException {
        var projectPath = Path.of(clonePath, project.getName());
        gitService.cloneGitRepo(projectPath.toString(), project.getLink(), project.getBranch());
        var executableFileName = builderInfoServices.get(project.getLanguage().toLowerCase(Locale.ROOT))
                .validateProjectAndGetExecutableFileName(projectPath.toString());
        project.setExecutableName(executableFileName);
        if (!DOCKER_LANG.equals(project.getLanguage())) {
            dockerfileBuilderService.createDockerFile(projectPath.resolve("Dockerfile"),
                    project.getLanguage().toLowerCase(Locale.ROOT), executableFileName, "");
        }
        dockerImageBuilderService.pushImageToRegistry(project);
        //TODO: delete tmp files
        projectRep.save(project);//сначала сохраняем, чтобы id сгенерировалось
        project.setKubernetesName("project" + project.getId());
        generateProjectNodePort(project);
        kubernetesService.createKubernetesObjects(project);
        //TODO: clear dockerhub repository
    }

    @Override
    public void updateProject(Project updatedProject) throws DeployException {
        Project project = projectRep.findProjectById(updatedProject.getId());
        project.setName(updatedProject.getName());
        project.setLink(updatedProject.getLink());
        project.setBranch(updatedProject.getBranch());
        project.setLanguage(updatedProject.getLanguage());
        project.setCurrentLaunch(updatedProject.getCurrentLaunch());
        if (project.userFinishesDeploy()) {
            kubernetesService.setDeploymentReplicas(project,0);
            project.setStatus(ProjectStatus.STOPPED);
        }
        if (project.userStartsDeploy()) {
            kubernetesService.setDeploymentReplicas(project,1);
            project.setStatus(ProjectStatus.RUNNING);
        }
        project.setLastLaunch(project.getCurrentLaunch());//После проверки на изменение состояния деплоя, обновляем буфферную переменную для следующих проверок
        projectRep.save(project);
    }

    public void deleteProject(Project project) throws KuberException {
        kubernetesService.deleteKubernetesObjects(project);
        projectRep.delete(project);
    }

    @Override
    public Project getProjectDetails(Long projectId) {
        return null;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRep.findAll();
    }

    @Override
    public Project findProjectById(Long projectId) {
        return projectRep.findProjectById(projectId);
    }

    public void generateProjectNodePort(Project project) {
        Random random = new Random();
        while (project.getNodePort() == null) {
            Integer nodePort = 30000 + random.nextInt(2767);
            if (!projectRep.existsByNodePort(nodePort))
                project.setNodePort(nodePort);
        }
    }
}
