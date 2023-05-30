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

import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

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
        String commitId = gitService.cloneGitRepo(sourceDir.toFile(),
                project.getGitUrl(),
                project.getGitBranch());
        project.setCommitHash(commitId);
        if (!Objects.equals(project.getType(), "DOCKER")) {
            String runArgs = String.join(", ", project.getEnvs());
            dockerfileBuilderService.createDockerFile(sourceDir, project.getType(), "app", runArgs);
        }

        kubernetesService.createKanikoPod(project);
        log.info("Very good");
        project.setStatus(ProjectStatus.ACTIVE);
        projectRep.save(project);

//            log.info(String.format("Старт деплоя проекта %s", project.getName()));
//            dockerImageBuilderService.pushImageToRegistry(project);
//            cleanerService.cleanCachedLibs(Path.of(clonePath, project.getName()).toString());
//            projectRep.save(project);//сначала сохраняем, чтобы id сгенерировалось
//            project.setKubernetesName("project" + project.getId());
//            generateProjectNodePort(project);
//            kubernetesService.createKubernetesObjects(project);
//            //TODO: clear dockerhub repository
//            project.setStatus(ProjectStatus.ACTIVE);
//            projectRep.save(project);
//            log.info(String.format("Проект %s успешно задеплоен", project.getName()));
    }

    @Async
    @SneakyThrows
    public void redeployProject(Project project) throws DeployException {
        //TODO delete kube objects if exist
        deployProject(project);
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
