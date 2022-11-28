package ru.freemiumhosting.master.service.impl;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategyBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KubernetesService {
    private final ProjectRep projectRep;

    @Value("${freemium.hosting.kubeconfig}")
    private String kubeConfigPath;

    public KubernetesClient createKubernetesApiClient() {
        System.setProperty("kubeconfig", kubeConfigPath);
        return new KubernetesClientBuilder().build();
    }

    public void createDeployment(KubernetesClient client, Project project) {
        try {
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
                    .withName(project.getExecutableName())
                    .withImage(project.getRegistryDestination())
                    .addNewPort()
                    .withContainerPort(8080)
                    .endPort()
                    .endContainer()
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            client.apps().deployments().inNamespace("user1").create(deployment);

        } catch (KubernetesClientException e) {
            project.setStatus(ProjectStatus.ERROR);
            projectRep.save(project);
            log.error("При деплое проекта произошла ошибка", e);
        }
    }

    public void createService(KubernetesClient client, Project project) {
        try {

            HashMap<String, String> annotations = new HashMap<>();
            annotations.put("name", project.getKubernetesName());
            HashMap<String, String> labels = new HashMap<>();
            labels.put("app.kubernetes.io/name", project.getKubernetesName());
            io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName(project.getKubernetesName())
                    .withNamespace("user1")
                    .withAnnotations(annotations)
                    .withLabels(labels)
                    .endMetadata()
                    .withNewSpec()
                    .withType("NodePort")
                    .withPorts()
                    .addNewPort()
                    .withPort(8080)
                    .withNodePort(project.getNodePort())
                    .endPort()
                    .withSelector(labels)
                    .endSpec()
                    .build();

            client.services().inNamespace("user1").create(service);

        } catch (KubernetesClientException e) {
            project.setStatus(ProjectStatus.ERROR);
            projectRep.save(project);
            log.error("При деплое проекта произошла ошибка", e);
        }
    }


    public void createNamespaceIfDontExist(KubernetesClient client, Project project) {
        try {
            NamespaceList namespaceList = client.namespaces().list();
            List<String> list =
                    namespaceList
                            .getItems()
                            .stream()
                            .map(v1Namespace -> v1Namespace.getMetadata().getName())
                            .collect(Collectors.toList());
            if (!list.contains("user1")) {
                Namespace ns = new NamespaceBuilder()
                        .withNewMetadata()
                        .withName("user1")
                        .endMetadata()
                        .build();
                client.namespaces().create(ns);
            }
        } catch (KubernetesClientException e) {
            project.setStatus(ProjectStatus.ERROR);
            projectRep.save(project);
            log.error("При деплое проекта произошла ошибка", e);
        }
    }
        public void createKubernetesObjects(Project project){
            try {
                KubernetesClient client = createKubernetesApiClient();
                createNamespaceIfDontExist(client, project);
                createService(client, project);
                createDeployment(client, project);
            } catch (KubernetesClientException e) {
                project.setStatus(ProjectStatus.ERROR);
                projectRep.save(project);
                log.error("При деплое проекта произошла ошибка", e);
            }
        }

}
