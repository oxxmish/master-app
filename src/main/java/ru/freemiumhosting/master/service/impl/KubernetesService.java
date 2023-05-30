package ru.freemiumhosting.master.service.impl;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategyBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Logs;
import ru.freemiumhosting.master.repository.LogsRepository;
import ru.freemiumhosting.master.utils.exception.KuberException;
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
    @Value("${freemium.hosting.containerPort}")
    private Integer containerPort;

    @Value("user1")
    private String namespace;

    public void createKanikoPod(Project project) {
        String[] kanikoArgs = createKanikoArgs(project);

        Pod kanikoPod = new PodBuilder().withNewMetadata()
                .withName(String.format("kaniko-%s", project.getCommitHash()))
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
                .withName(String.format("kaniko-%s", project.getCommitHash()))
                .withImage("gcr.io/kaniko-project/executor:debug")
                .withArgs(kanikoArgs)
                .withVolumeMounts(
                        new VolumeMountBuilder().withName("kaniko-secret").withMountPath("/kaniko/.docker").build(),
                        new VolumeMountBuilder().withName("build-pv-storage").withMountPath("/build").build()
                )
                .endContainer()
                .endSpec()
                .build();

        Pod pod = kubernetesClient.pods().inNamespace("default").resource(kanikoPod).create();
        Logs logs = new Logs(project.getId(), kubernetesClient.pods().resource(pod).getLog(true));

    }

    private String[] createKanikoArgs(Project project) {
        return new String[] {
            "--dockerfile=Dockerfile",
            String.format("--context=dir://%s/%s/%s", clonePath, project.getOwnerName(), project.getName()),
            String.format("--destination=freemiumhosting/%s-%s:%s", project.getOwnerName(), project.getName(), project.getCommitHash()),
            "--verbosity=debug"
        };
    }

    public void createDeployment(KubernetesClient client, Project project) {
        List<EnvVar> envs = Collections.emptyList();
        if (project.getEnvs() != null)
            envs = envService.getEnvsByProject(project).entrySet().stream()
                    .map(entry -> new EnvVarBuilder().withName(entry.getKey()).withValue(entry.getValue()).build()).collect(Collectors.toList());
        log.info("envs ", envs);
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
                .withName("app")
                .withEnv(envs)
                .withImage(project.getRegistryDestination())
                //.withImage("nginx")//для теста локально
                .addNewPort()
                .withContainerPort(containerPort)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.apps().deployments().inNamespace(namespace).create(deployment);
    }

    public void createService(KubernetesClient client, Project project) {
        HashMap<String, String> annotations = new HashMap<>();
        annotations.put("name", project.getKubernetesName());
        HashMap<String, String> labels = new HashMap<>();
        labels.put("app.kubernetes.io/name", project.getKubernetesName());
        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(project.getKubernetesName())
                .withNamespace(namespace)
                .withAnnotations(annotations)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withType("NodePort")
                .withPorts()
                .addNewPort()
                .withPort(containerPort)
                .withNodePort(project.getNodePort())
                .endPort()
                .withSelector(labels)
                .endSpec()
                .build();

        client.services().inNamespace(namespace).create(service);
    }


    public void createNamespaceIfDontExist(KubernetesClient client, Project project) {
        NamespaceList namespaceList = client.namespaces().list();
        List<String> list =
                namespaceList
                        .getItems()
                        .stream()
                        .map(v1Namespace -> v1Namespace.getMetadata().getName())
                        .collect(Collectors.toList());
        if (!list.contains(namespace)) {
            Namespace ns = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespace)
                    .endMetadata()
                    .build();
            client.namespaces().create(ns);
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
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        projectRep.save(project);
        //TODO replicas = 1
        Thread.sleep(3000);
        project.setStatus(ProjectStatus.ACTIVE);
        projectRep.save(project);
    }

    @Async
    @SneakyThrows
    public void stopProject(Project project) {
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        projectRep.save(project);
        //TODO replicas = 0
        Thread.sleep(3000);
        project.setStatus(ProjectStatus.STOPPED);
        projectRep.save(project);
    }
}
