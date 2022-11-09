package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.BuilderInfoService;
import ru.freemiumhosting.master.service.ProjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    @Value("${freemium.hosting.git-clone-path}")
    private String clonePath;
    private final GitService gitService;
    private final DockerfileBuilderService dockerfileBuilderService;
    private final BuilderInfoService builderInfoService;
    private final ProjectRep projectRep;

    @Override
    public void createProject(Project project) {
        gitService.cloneGitRepo(project.getLink());
        //TODO сделать автоматический поиск помника
        String jarFileName = builderInfoService.getJarFileName(clonePath + "\\master");
        //TODO понять откуда брать параметры запуска
        dockerfileBuilderService.createDockerFile(project.getLanguage(), jarFileName, "");
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
    public List<Project> getAllProjects(Long projectId) {
        return null;
    }

    @Override
    public Project findProjectById(Long projectId) {
        return projectRep.findProjectById(projectId);
    }
}
