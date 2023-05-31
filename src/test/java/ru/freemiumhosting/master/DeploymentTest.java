package ru.freemiumhosting.master;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.DeployService;

import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Disabled
public class DeploymentTest {
    @Autowired
    ProjectRep projectRep;

    @Autowired
    DeployService deployService;
    @Test
    void testKubernetesDeployment() {
        Project project = new Project();
        project.setOwnerName("user1");
        project.setId(37L);
        project.setPorts(List.of("9000"));
        project.setRegistryDestination("freemiumhosting/user1-java:06d4c6305d286c86c16aed126bee7f9afce836b4");

        deployService.createKubernetesObj(project);
    }
}
