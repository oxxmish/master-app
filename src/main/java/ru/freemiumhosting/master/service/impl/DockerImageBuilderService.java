package ru.freemiumhosting.master.service.impl;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.utils.exception.GitCloneException;
import ru.freemiumhosting.master.model.Project;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerImageBuilderService {
    private final KubernetesService kubernetesService;
    private final GitService gitService;
    private final DockerfileBuilderService dockerfileBuilderService;
    private final ProjectRep projectRep;
    @Value("${freemium.hosting.git-clone-path}")
    String clonePath;

    public Project buildProject(Project project) {
        String sourceDirName = String.valueOf(Instant.now().getEpochSecond());
        Path sourceDir = Path.of(clonePath, project.getOwnerName(), sourceDirName);
        try {
            downloadSources(project, sourceDir);
            kubernetesService.createKanikoPodAndDelete(project, sourceDirName);
        } catch (Exception e) {
            logAndCleanResources(project);
        }
        return projectRep.save(project);
    }

    private void logAndCleanResources(Project project) {
        log.error("Error while building project " + project.getName());
        kubernetesService.deleteKanikoPod(project);
        project.setStatus(ProjectStatus.ERROR);
        projectRep.save(project);
        throw new DeployException("Ошибки при сборке образа");
    }

    private void downloadSources(Project project, Path sourceDir) throws GitCloneException {
        String commitId = gitService.cloneGitRepo(sourceDir.toFile(),
                project.getGitUrl(),
                project.getGitBranch());
        project.setCommitHash(commitId);
        if (!Objects.equals(project.getType(), "DOCKER")) {
            String runArgs = String.join(", ", project.getEnvs());
            dockerfileBuilderService.createDockerFile(sourceDir, project.getType(), "app", runArgs);
        }
    }

}
