package ru.freemiumhosting.master;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.service.impl.DockerImageBuilderService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
class DockerImageBuilderTest {

    @Autowired
    DockerImageBuilderService dockerImageBuilderService;

    @Test
    void test() {
        dockerImageBuilderService.startDeploy();
    }

}
