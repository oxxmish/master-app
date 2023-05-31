package ru.freemiumhosting.master.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.service.impl.DockerImageBuilderService;
import ru.freemiumhosting.master.service.impl.DockerfileBuilderService;
import ru.freemiumhosting.master.service.impl.GitService;
import ru.freemiumhosting.master.service.impl.KubernetesService;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.utils.exception.GitCloneException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployService {
    private final DockerImageBuilderService dockerImageBuilderService;
    private final DockerfileBuilderService dockerfileBuilderService;
    private final GitService gitService;
    private final CleanerService cleanerService;
    private final ProjectRep projectRep;
    private final KubernetesService kubernetesService;
    @Value("${freemium.hosting.git-clone-path}")
    String clonePath;

    @Async
    @SneakyThrows
    public void deployProject(Project project) throws DeployException {
        Path sourceDir = Path.of(clonePath, project.getOwnerName(), project.getName());
        downloadSources(project, sourceDir);
        kubernetesService.createKanikoPodAndDelete(project);
        cleanProjectDir(sourceDir);

        project.setKubernetesName(String.format("%s-%s", project.getOwnerName(), project.getId() ));
        kubernetesService.createNamespaceIfDontExist(project);
        kubernetesService.createOrReplaceService(project);
        kubernetesService.createOrReplaceDeployment(project);
        project.setStatus(ProjectStatus.ACTIVE);
        projectRep.save(project);
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

    @SneakyThrows
    private void cleanProjectDir(Path sourceDir) {
        Files.deleteIfExists(sourceDir);
    }

    @Async
    @SneakyThrows
    public void redeployProject(Project project) throws DeployException {
        //TODO delete kube objects if exist
        deployProject(project);
    }

}
