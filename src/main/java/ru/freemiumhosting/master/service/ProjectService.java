package ru.freemiumhosting.master.service;

import java.util.List;
import ru.freemiumhosting.master.exception.DeployException;
import ru.freemiumhosting.master.model.Project;

public interface ProjectService {
    void createProject(Project project) throws DeployException;
    void deployProject(Project project) throws DeployException;
    void updateProject(Project project) throws DeployException;
    Project getProjectDetails(Long projectId);
    List<Project> getAllProjects();
    Project findProjectById(Long projectId);
    void generateProjectNodePort(Project project);
}
