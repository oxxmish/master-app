package ru.freemiumhosting.master.service;

import java.util.List;

import ru.freemiumhosting.master.dto.ProjectDto;
import ru.freemiumhosting.master.exception.DeployException;
import ru.freemiumhosting.master.exception.KuberException;
import ru.freemiumhosting.master.model.Project;

public interface ProjectService {
    void createProject(Project project) throws DeployException;
    void deployProject(Project project) throws DeployException;
    void updateDeploy(Project project) throws DeployException;
    void updateProject(ProjectDto projectDto) throws DeployException;
    void createProject(ProjectDto projectDto) throws DeployException;

    void deleteProject(Project project) throws KuberException;
    Project getProjectDetails(Long projectId);
    List<Project> getAllProjects();
    Project findProjectById(Long projectId);
}
