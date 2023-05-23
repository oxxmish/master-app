package ru.freemiumhosting.master.service;

import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.model.Project;

public interface DeployService {
    void deployProject(Project project) throws DeployException;
}
