package ru.freemiumhosting.master.service.impl;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategyBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Logs;
import ru.freemiumhosting.master.repository.LogsRepository;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;

@Slf4j
@Service
@RequiredArgsConstructor
public class KubernetesService {
    private final ProjectRep projectRep;
    private final EnvService envService;
    private final KubernetesClient kubernetesClient;
    private final LogsRepository logsRepository;
    @Value("${freemium.hosting.git-clone-path}")
    String clonePath;

    public void createKanikoPodAndDelete(Project project, String sourceDir) {
        String destinationImage = String.format("freemiumhosting/%s-%s:%s", project.getOwnerName(), project.getName(), project.getCommitHash());
        project.setRegistryDestination(destinationImage);
        String[] kanikoArgs = createKanikoArgs(project, sourceDir);

        Pod kanikoPod = new PodBuilder().withNewMetadata()
                .withName(getKanikoPodName(project))
                .endMetadata()
                .withNewSpec()
                .withVolumes(
                        new VolumeBuilder().withName("build-pv-storage")
                                .withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder().withClaimName("build-pv-claim").build()).build(),
                        new VolumeBuilder().withName("kaniko-secret")
                                .withSecret(new SecretVolumeSourceBuilder().withSecretName("regcred")
                                        .withItems(new KeyToPathBuilder().withKey(".dockerconfigjson").withPath("config.json").build()
                                        ).build()
                                ).build()
                )
                .withRestartPolicy("Never")
                .addNewContainer()
                .withName(getKanikoPodName(project))
                .withImage("gcr.io/kaniko-project/executor:debug")
                .withArgs(kanikoArgs)
                .withVolumeMounts(
                        new VolumeMountBuilder().withName("kaniko-secret").withMountPath("/kaniko/.docker").build(),
                        new VolumeMountBuilder().withName("build-pv-storage").withMountPath("/build").build()
                )
                .endContainer()
                .endSpec()
                .build();


        kubernetesClient.pods().inNamespace("default").resource(kanikoPod).createOrReplace();

        //wait for finish or error
        kubernetesClient.pods().inNamespace("default")
                .withName(getKanikoPodName(project))
                .waitUntilCondition(pod1 -> pod1.getStatus().getPhase().equals("Succeeded")
                        || pod1.getStatus().getPhase().equals("Error"), 2, TimeUnit.MINUTES);


        String logMessage = kubernetesClient.pods().inNamespace("default").withName(getKanikoPodName(project)).getLog(true);
        Logs logs = new Logs(project.getId(), logMessage);
        logsRepository.save(logs);
        deleteKanikoPod(project);
    }

    public void deleteKanikoPod(Project project) {
        kubernetesClient.pods().inNamespace("default").withName(getKanikoPodName(project)).delete();
    }

    private static String getKanikoPodName(Project project) {
        return String.format("kaniko-%s", project.getCommitHash());
    }

    private String[] createKanikoArgs(Project project, String sourceDir) {
        return new String[]{
                "--dockerfile=Dockerfile",
                String.format("--context=dir://%s/%s/%s", clonePath, project.getOwnerName(), sourceDir),
                String.format("--destination=%s", project.getRegistryDestination()),
                "--verbosity=debug"
        };
    }

    public void createOrReplaceDeployment(Project project) {
        List<EnvVar> envs = Collections.emptyList();
        if (project.getEnvs() != null)
            envs = envService.getEnvsByProject(project).entrySet().stream()
                    .map(entry -> new EnvVarBuilder().withName(entry.getKey()).withValue(entry.getValue()).build()).collect(Collectors.toList());
        log.info("envs ", envs);

        //TODO переделать
        String containerLocalPortString = project.getPorts().stream().findFirst().orElse("80");
        Integer containerLocalPort = Integer.valueOf(containerLocalPortString);

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(project.getKubernetesName())
                .addToAnnotations("name", project.getKubernetesName())
                .endMetadata()
                .withNewSpec()
                .withStrategy(new DeploymentStrategyBuilder().withType("Recreate").build())
                .withReplicas(1)
                .withNewSelector()
                .addToMatchLabels("app.kubernetes.io/name", project.getKubernetesName())
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels("app.kubernetes.io/name", project.getKubernetesName())
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("deployment-"+project.getKubernetesName())
                .withEnv(envs)
                .withImage(project.getRegistryDestination())
                .addNewPort()
                .withContainerPort(containerLocalPort)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        kubernetesClient.apps().deployments().inNamespace(project.getOwnerName()).resource(deployment).createOrReplace();
    }

    public void createOrReplaceService(Project project) {
        HashMap<String, String> annotations = new HashMap<>();
        annotations.put("name", project.getKubernetesName());
        HashMap<String, String> labels = new HashMap<>();
        labels.put("app.kubernetes.io/name", project.getKubernetesName());

        //TODO переделать
        String containerLocalPortString = project.getPorts().stream().findFirst().orElse("80");
        Integer containerLocalPort = Integer.valueOf(containerLocalPortString);
        generateProjectNodePort(project);

        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(project.getKubernetesName())
                .withNamespace(project.getOwnerName())
                .withAnnotations(annotations)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withType("NodePort")
                .withPorts()
                .addNewPort()
                .withPort(containerLocalPort)
                .withNodePort(project.getNodePort())
                .endPort()
                .withSelector(labels)
                .endSpec()
                .build();

        kubernetesClient.services().inNamespace(project.getOwnerName()).resource(service).createOrReplace();
    }


    public void createNamespaceIfDontExist(Project project) {
        boolean isUserNamespaceNotExist = kubernetesClient.namespaces()
                .list()
                .getItems().
                stream().map(Namespace::getMetadata)
                .map(ObjectMeta::getName)
                .noneMatch(i -> i.equals(project.getOwnerName()));
        if (isUserNamespaceNotExist) {
            Namespace ns = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(project.getOwnerName())
                    .endMetadata()
                    .build();
            kubernetesClient.namespaces().resource(ns).create();
        }
    }

    public void generateProjectNodePort(Project project) {
        Random random = new Random();
        while (project.getNodePort() == null) {
            Integer nodePort = 30000 + random.nextInt(2767);
            if (!projectRep.existsByNodePort(nodePort)) {
                project.setNodePort(nodePort);
            }
        }
    }

//    public void createKubernetesObjects(Project project) throws KuberException {
//        try {
//            log.info("Generate kuber objects for project {}", project.getName());
//            KubernetesClient client = createKubernetesApiClient();
//            createNamespaceIfDontExist(client, project);
//            createService(client, project);
//            createDeployment(client, project);
//            project.setStatus(ProjectStatus.ACTIVE);
//            projectRep.save(project);
//        } catch (Exception e) {
//            project.setStatus(ProjectStatus.ERROR);
//            projectRep.save(project);
//            log.error("При деплое проекта произошла ошибка", e);
//            throw new KuberException("При деплое проекта произошла ошибка");
//        }
//    }

//    public void deleteKubernetesObjects(Project project) throws KuberException {
//        try {
//            KubernetesClient client = createKubernetesApiClient();
//            client.apps().deployments().inNamespace(namespace).withName(project.getKubernetesName()).delete();
//            client.services().inNamespace(namespace).withName(project.getKubernetesName()).delete();
//        } catch (Exception e) {
////            project.setStatus(ProjectStatus.ERROR);
//            log.error("При удалении проекта произошла ошибка", e);
////            throw new KuberException("При удалении проекта произошла ошибка");
//        }
//    }

//    public void setDeploymentReplicas(Project project, Integer replicasNumber)
//            throws KuberException {
//        try {
//            KubernetesClient client = createKubernetesApiClient();
//            client.apps().deployments().inNamespace(namespace).withName(project.getKubernetesName()).scale(replicasNumber);
//        } catch (Exception e) {
//            project.setStatus(ProjectStatus.ERROR);
//            log.error("При изменении проекта произошла ошибка", e);
//            throw new KuberException("При изменении проекта произошла ошибка");
//        }
//    }

    @Async
    @SneakyThrows
    public void startProject(Project project) {
        kubernetesClient.apps().deployments()
                .inNamespace(project.getOwnerName())
                .withName(project.getKubernetesName()).scale(1);
        project.setStatus(ProjectStatus.ACTIVE);
        projectRep.save(project);
    }

    @Async
    @SneakyThrows
    public void stopProject(Project project) {
        kubernetesClient.apps().deployments()
                .inNamespace(project.getOwnerName())
                .withName(project.getKubernetesName()).scale(0);
        project.setStatus(ProjectStatus.STOPPED);
        projectRep.save(project);
    }
}
