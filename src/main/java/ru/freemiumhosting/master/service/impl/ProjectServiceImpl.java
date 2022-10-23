package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
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

    @Override
    public void createProject(Project project) {
        gitService.cloneGitRepo(project.getLink());
        //TODO сделать автоматический поиск помника
        String jarFileName = builderInfoService.getJarFileName(clonePath + "\\master");
        //TODO понять откуда брать параметры запуска
        dockerfileBuilderService.createJavaDockerFile(jarFileName, "-Dserver.port=8081");
    }

    @Override
    public void deployProject(Project project) {

    }

    @Override
    public Project getProjectDetails(Long projectId) {
        return null;
    }

    @Override
    public List<Project> getAllProjects(Long projectId) {
        return null;
    }
}
