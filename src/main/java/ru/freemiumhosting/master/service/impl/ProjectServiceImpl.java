package ru.freemiumhosting.master.service.impl;

import static ru.freemiumhosting.master.service.builderinfo.DockerInfoService.DOCKER_LANG;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;
import ru.freemiumhosting.master.service.ProjectService;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final String clonePath;
    private final GitService gitService;
    private final DockerfileBuilderService dockerfileBuilderService;
    // key - language, value - BuilderInfoService
    private final Map<String, BuilderInfoService> builderInfoServices;
    private final ProjectRep projectRep;

    public ProjectServiceImpl(@Value("${freemium.hosting.git-clone-path}")String clonePath,
                              GitService gitService,
                              DockerfileBuilderService dockerfileBuilderService,
                              Collection<BuilderInfoService> builderInfoServices, ProjectRep projectRep) {
        this.clonePath = clonePath;
        this.gitService = gitService;
        this.dockerfileBuilderService = dockerfileBuilderService;
        this.builderInfoServices = builderInfoServices.stream().collect(Collectors.toMap(builderInfoService ->
                builderInfoService.supportedLanguage().toLowerCase(Locale.ROOT), s -> s));
        this.projectRep = projectRep;
    }

    @Override
    @SneakyThrows //TODO: handle InvalidProjectException
    public void createProject(Project project) {
        var projectPath = Path.of(clonePath, project.getName());
        gitService.cloneGitRepo(projectPath.toString(), project.getLink(), project.getBranch());
        var executableFileName = builderInfoServices.get(project.getLanguage().toLowerCase(Locale.ROOT))
            .validateProjectAndGetExecutableFileName(projectPath.toString());
        if (!DOCKER_LANG.equals(project.getLanguage())) {
            dockerfileBuilderService.createDockerFile(projectPath.resolve("Dockerfile"),
                project.getLanguage().toLowerCase(Locale.ROOT), executableFileName, "");
        }
        projectRep.save(project);
    }

    @Override
    public void deployProject(Project project) {

    }

    @Override
    public void updateProject(Project project) {
        if(project.userFinishesDeploy()){
            //TODO: вызываем сервис по сворачиванию проекта
            project.setStatus("Деплой приостановлен пользователем");
        }
        if(project.userStartsDeploy()){
            //TODO: вызываем сервис по развертыванию проекта
            project.setStatus("Деплой проекта запущен успешно");
        }
        project.setLastLaunch(project.getCurrentLaunch());//После проверки на изменение состояния деплоя, обновляем буфферную переменную для следующих проверок
        projectRep.save(project);
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
}
