package ru.freemiumhosting.master.service;


import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.model.dto.AdminViewDto;
import ru.freemiumhosting.master.model.dto.ClusterStatisticsDto;
import ru.freemiumhosting.master.model.dto.LogsDto;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.security.SecurityUser;
import ru.freemiumhosting.master.service.impl.*;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.utils.exception.InvalidProjectException;
import ru.freemiumhosting.master.utils.exception.KuberException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;
import ru.freemiumhosting.master.utils.mappers.ProjectMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Slf4j
@Service
public class ProjectService {

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
    private final ProjectMapper projectMapper = ProjectMapper.INSTANCE;


    public ProjectService(@Value("${freemium.hosting.git-clone-path}") String clonePath,
                              GitService gitService, EnvService envService,
                              @Value("${freemium.hosting.domain-name}") String domainName,
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

    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRep.findByIdAndOwnerId(projectId, SecurityUser.getCurrentUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Не найден запрашиваемый проект у текущего пользователя"));
        return projectMapper.projectToProjectDto(project);
    }

    public List<ProjectDto> getUsersProjects() {
        SecurityUser currentUser =  SecurityUser.getCurrentUser();
        List<Project> userProjects = projectRep.findByOwnerName(currentUser.getUsername());
        return userProjects.stream().map(projectMapper::projectToProjectDto).collect(Collectors.toList());
    }

    @SneakyThrows
    @Transactional
    public ProjectDto createProject(ProjectDto projectDto) throws DeployException {
        Project project = projectMapper.projectDtoToProject(projectDto);
        setUsersInfo(project);
        checkNameOfProject(project);
        projectRep.save(project);
        //TODO build docker image
        Thread.sleep(3000);
        //TODO start deploy
        return projectMapper.projectToProjectDto(project);
    }

    private void checkNameOfProject(Project project) {
        long check = projectRep.countByNameIgnoreCaseAndOwnerId(project.getName(), project.getOwnerId());
        if (check > 1) {
            throw new IllegalStateException("Проект с таким именем у пользователя существует");
        }
    }

    private void setUsersInfo(Project project) {
        SecurityUser currentUser = SecurityUser.getCurrentUser();
        project.setOwnerId(currentUser.getUserId());
        project.setOwnerName(currentUser.getUsername());
    }

    @Transactional
    public ProjectDto updateProject(ProjectDto projectDto) throws DeployException {
        checkProjectExistenceOrThrow(projectDto.getId());
        Project editedProject = projectMapper.projectDtoToProject(projectDto);
        setUsersInfo(editedProject);
        checkNameOfProject(editedProject);
        //TODO redeploy project
        projectRep.save(editedProject);
        return projectMapper.projectToProjectDto(editedProject);
    }

    @Transactional
    public void deleteProject(Long projectId) throws KuberException {
        checkProjectExistenceOrThrow(projectId);
        //TODO delete kuber objects
//        kubernetesService.deleteKubernetesObjects(project);
        projectRep.deleteById(projectId);
    }

    @Transactional
    @SneakyThrows
    public void rebuildProject(Long projectId) {
        Project project = checkProjectExistenceOrThrow(projectId);
        //TODO rebuild project
        Thread.sleep(2000);
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        projectRep.save(project);
        deployService.redeployProject(project);
    }

    public void startProject(Long id) {
        Project project = checkProjectExistenceOrThrow(id);
        if (!(project.getStatus() == ProjectStatus.STOPPED))
            throw new InvalidProjectException("Старт проекта можно осуществить только из статуса STOOPED");
        kubernetesService.startProject(project);
    }

    public void stopProject(Long id) {
        Project project = checkProjectExistenceOrThrow(id);
        if (!(project.getStatus() == ProjectStatus.ACTIVE))
            throw new InvalidProjectException("Стоп проекта можно осуществить только из статуса ACTIVE");
        kubernetesService.stopProject(project);
    }

    public AdminViewDto getAdminView() {
        List<ProjectDto> dtos = projectRep.findAll().stream().map(projectMapper::projectToProjectDto).collect(Collectors.toList());
        return new AdminViewDto(new ClusterStatisticsDto(), dtos);
    }

    public LogsDto getLogs(Long id) {
        Project project = checkProjectExistenceOrThrow(id);
        if (project.getStatus() == ProjectStatus.ACTIVE) {
            //TODO get kubernetes logs
            return new LogsDto("Project logs");
        } else {
            //TODO get kaniko logs
            return new LogsDto("Build logs");
        }
    }

    private Project checkProjectExistenceOrThrow(Long projectId) {
        return projectRep.findByIdAndOwnerId(projectId, SecurityUser.getCurrentUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Не найден редактируемый проект у текущего пользователя"));
    }
//    @Override
//    public void deployProject(Project project) throws DeployException {
//        var projectPath = Path.of(clonePath, project.getName());
//        String commitHash = gitService.cloneGitRepo(projectPath.toString(), project.getLink(), project.getBranch());
//        project.setCommitHash(commitHash);
//        var executableFileName = builderInfoServices.get(project.getLanguage().toLowerCase(Locale.ROOT))
//                .validateProjectAndGetExecutableFileName(projectPath.toString());
//        project.setExecutableName(executableFileName);
//        if (!DOCKER_LANG.equals(project.getLanguage())) {
//            dockerfileBuilderService.createDockerFile(projectPath.resolve("Dockerfile"),
//                    project.getLanguage().toLowerCase(Locale.ROOT), executableFileName, "");
//        }
//        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
//        project.setCurrentLaunch("true");
//        projectRep.save(project);
//        deployService.deployProject(project);
//    }
//
//    public void updateDeploy(Project project) throws DeployException {
//        kubernetesService.deleteKubernetesObjects(project);
//        deployProject(project);
//        if (project.getCurrentLaunch().equals("false")) {
//            kubernetesService.setDeploymentReplicas(project,0);
//            project.setStatus(ProjectStatus.STOPPED);
//        }
//        else if (project.getCurrentLaunch().equals("true")) {
//            kubernetesService.setDeploymentReplicas(project,1);
//            project.setStatus(ProjectStatus.RUNNING);

//        }


//    public Project findProjectById(Long projectId) {
//        return projectRep.findProjectById(projectId);

//

//    }
//
//    public void generateProjectNodePort(Project project) {
//        Random random = new Random();
//        while (project.getNodePort() == null) {
//            Integer nodePort = 30000 + random.nextInt(2767);
//            if (!projectRep.existsByNodePort(nodePort))
//                project.setNodePort(nodePort);
//        }
//    }
}
