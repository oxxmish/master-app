package ru.freemiumhosting.master.service.impl;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class KubernetesService1 {
    public KubernetesClient createKubernetesApiClient() {
        System.setProperty("kubeconfig", "src/main/resources/configs/config.yml");
        String kubeConfigPath = "src/main/resources/configs/config.yml";
        KubernetesClient client = new KubernetesClientBuilder().build();

        return client;
    }

    public void createKubernetesObject() throws IOException {
        KubernetesClient client = createKubernetesApiClient();

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName("nginx")
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withNewTemplate()
                .withNewMetadata()
                .addToLabels("app", "nginx")
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("nginx")
                .withImage("nginx")
                .addNewPort()
                .withContainerPort(80)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .withNewSelector()
                .addToMatchLabels("app", "nginx")
                .endSelector()
                .endSpec()
                .build();

        client.apps().deployments().inNamespace("user1").toString();
        client.pods().inNamespace("user1").withName("").create();
    }
}
