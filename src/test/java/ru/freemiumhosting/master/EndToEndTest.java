package ru.freemiumhosting.master;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.model.Project;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EndToEndTest {
    @Autowired
    ProjectService projectService;

    @Test
    void test() throws DeployException {
        Project project = new Project();
        project.setName("Test");
        project.setLink("https://github.com/dartrhevan/test-app.git");
        project.setBranch("java-app");
        project.setLanguage("java");
        projectService.createProject(project);
    }

}
