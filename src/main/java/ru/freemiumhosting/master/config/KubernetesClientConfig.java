package ru.freemiumhosting.master.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesClientConfig {
    @Value("${freemium.hosting.kubeconfig}")
    private String kubeConfigPath;

    @Bean
    public KubernetesClient kubernetesClient() {
        System.setProperty("kubeconfig", kubeConfigPath);
        return new KubernetesClientBuilder().build();
    }
}
