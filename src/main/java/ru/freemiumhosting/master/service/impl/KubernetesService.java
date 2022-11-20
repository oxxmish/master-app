package ru.freemiumhosting.master.service.impl;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.ProjectService;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KubernetesService {

    private final ProjectRep projectRep;

    public ApiClient createKubernetesApiClient() throws IOException, ApiException {
        //String kubeConfigPath = System.getenv("HOME") + "/.kube/config";
        String kubeConfigPath = "src/main/resources/configs/config.yml";

        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        return client;
    }

    public void createKubernetesObject(Project project) {
        try {
            ApiClient client = createKubernetesApiClient();

            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();

            V1Pod pod =
                    new V1Pod()
                            .metadata(new V1ObjectMeta().name("test-pod"))
                            .spec(new V1PodSpec().containers(Arrays.asList(new V1Container().name("www").image("nginx:1.23.2-alpine"))))
                            .spec(new V1PodSpec().containers(Arrays.asList(new V1Container().name("w").addEnvItem(new V1EnvVar().name("number").value("77")))));
            V1Pod pod1 =
                    new V1Pod()
                            .metadata(new V1ObjectMeta().name("anotherpod"))
                            .spec(new V1PodSpec().containers(Arrays.asList(new V1Container().name("www").image("nginx:1.23.2-alpine"))));

            //api.createNamespacedPod("default", pod, null, null, null, null);
            V1NamespaceList namespaceList = api.listNamespace(null, null, null, null, null, null, null, null, null, null);
            List<String> list =
                    namespaceList
                            .getItems()
                            .stream()
                            .map(v1Namespace -> v1Namespace.getMetadata().getName())
                            .collect(Collectors.toList());
            if (!list.contains("user1"))//TODO: CHANGE
                api.createNamespace(new V1Namespace().metadata(new V1ObjectMeta().name("user1")), null, null, null, null);

            HashMap<String, String> ii = new HashMap<>();
            ii.put("app", "deployment");
            ArrayList<V1ServicePort> ports = new ArrayList<>();
            ports.add(new V1ServicePort().port(8080).nodePort(project.getNodePort()));//TODO:CHANGE 8080
            api.createNamespacedService("user1", new V1Service()
                    .apiVersion("v1")
                    //.kind("Deployment")
                    .metadata(new V1ObjectMeta().name(project.getKubernetesName())).spec(new V1ServiceSpec().ports(ports).type("NodePort")), null, null, null, null);
        } catch (ApiException e) {
            project.setStatus("При деплое проекта произошла ошибка");
            projectRep.save(project);
            System.out.println(e.getResponseBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
