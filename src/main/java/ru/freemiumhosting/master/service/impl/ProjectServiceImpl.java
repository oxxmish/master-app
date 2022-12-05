package ru.freemiumhosting.master.service.impl;

import static ru.freemiumhosting.master.service.builderinfo.DockerInfoService.DOCKER_LANG;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.dto.ProjectDto;
import ru.freemiumhosting.master.exception.DeployException;
import ru.freemiumhosting.master.exception.KuberException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.DeployService;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;
import ru.freemiumhosting.master.service.ProjectService;
import ru.freemiumhosting.master.service.CleanerService;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final String clonePath;
    private final String domainName;
    private final GitService gitService;
    private final EnvService envService;
    private final KubernetesService kubernetesService;
    private final DockerfileBuilderService dockerfileBuilderService;
    // key - language, value - BuilderInfoService
    private final Map<String, BuilderInfoService> builderInfoServices;
    private final DockerImageBuilderService dockerImageBuilderService;
    private final CleanerService cleanerService;
    private final DeployService deployService;
    private final ProjectRep projectRep;


    public ProjectServiceImpl(@Value("${freemium.hosting.git-clone-path}") String clonePath,
                              GitService gitService, EnvService envService,@Value("${freemium.hosting.registry.default-repo}") String domainName,
                              KubernetesService kubernetesService, DockerfileBuilderService dockerfileBuilderService,
                              Collection<BuilderInfoService> builderInfoServices,
                              DockerImageBuilderService dockerImageBuilderService,
                              CleanerService cleanerService, DeployService deployService, ProjectRep projectRep) {
        this.clonePath = clonePath;
        this.domainName=domainName;
        this.gitService = gitService;
        this.envService = envService;
        this.kubernetesService = kubernetesService;
        this.dockerfileBuilderService = dockerfileBuilderService;
        this.dockerImageBuilderService = dockerImageBuilderService;
        this.builderInfoServices = builderInfoServices.stream().collect(Collectors.toMap(builderInfoService ->
                builderInfoService.supportedLanguage().toLowerCase(Locale.ROOT), s -> s));
        this.cleanerService = cleanerService;
        this.deployService = deployService;
        this.projectRep = projectRep;
    }

    @Override
    public void createProject(ProjectDto projectDto) throws DeployException {
        Project project = new Project();
        project.setName(projectDto.getName());
        project.setLink(projectDto.getLink());
        project.setBranch(projectDto.getBranch());
        project.setLanguage(projectDto.getLanguage());
        project.setStatus(ProjectStatus.CREATED);
        //project.setCurrentLaunch(projectDto.getCurrentLaunch());
        //project.setCurrentLaunch(projectDto.getCurrentLaunch());
        projectRep.save(project);//сначала сохраняем, чтобы id сгенерировалось
        project.setKubernetesName("project" + project.getId());
        generateProjectNodePort(project);
        project.generateAppLink(domainName);
        projectRep.save(project);
        envService.createEnvs(projectDto.getEnvNames(),projectDto.getEnvValues(),project);
    }

    @Override
    public void createProject(Project project) throws DeployException {
        //deployProject(project);
        project.setStatus(ProjectStatus.CREATED);
        projectRep.save(project);//сначала сохраняем, чтобы id сгенерировалось
        project.setKubernetesName("project" + project.getId());
        generateProjectNodePort(project);
        project.generateAppLink(domainName);
        projectRep.save(project);
    }

    @Override
    public void deployProject(Project project) throws DeployException {
        deployService.deployProject(project);
    }
    public void updateDeploy(Project project) throws DeployException {
        kubernetesService.deleteKubernetesObjects(project);
        deployProject(project);
        if (project.getCurrentLaunch().equals("false")) {
            kubernetesService.setDeploymentReplicas(project,0);
            project.setStatus(ProjectStatus.STOPPED);
        }
        else if (project.getCurrentLaunch().equals("true")) {
            kubernetesService.setDeploymentReplicas(project,1);
            project.setStatus(ProjectStatus.RUNNING);
        }
    }

    @Override
    public void updateProject(ProjectDto projectDto) throws DeployException {
        Project project = projectRep.findProjectById(projectDto.getId());
        project.setName(projectDto.getName());
        project.setLink(projectDto.getLink());
        project.setBranch(projectDto.getBranch());
        project.setLanguage(projectDto.getLanguage());
        project.setCurrentLaunch(projectDto.getCurrentLaunch());
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
        envService.updateEnvs(projectDto.getEnvNames(),projectDto.getEnvValues(),project);
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
