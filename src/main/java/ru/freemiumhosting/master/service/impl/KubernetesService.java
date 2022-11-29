package ru.freemiumhosting.master.service.impl;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategyBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.repository.ProjectRep;

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
            .withName(project.getName())
            .withImage(project.getRegistryDestination())
            .addNewPort()
            .withContainerPort(80)
            .endPort()
            .endContainer()
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

        client.apps().deployments().inNamespace("user1").create(deployment);
    }

    public void createService(KubernetesClient client, Project project) {
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
    }


    public void createNamespaceIfDontExist(KubernetesClient client, Project project) {
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
    }

    public void createKubernetesObjects(Project project) {
        try {
            log.info("Generate kuber objects for project {}", project.getName());
            KubernetesClient client = createKubernetesApiClient();
            createNamespaceIfDontExist(client, project);
            createService(client, project);
            createDeployment(client, project);
            project.setStatus(ProjectStatus.RUNNING);
            projectRep.save(project);
        } catch (KubernetesClientException e) {
            project.setStatus(ProjectStatus.ERROR);
            projectRep.save(project);
            log.error("При деплое проекта произошла ошибка", e);
        }
    }

}
