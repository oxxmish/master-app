package ru.freemiumhosting.master;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.api.MainController;
import ru.freemiumhosting.master.model.Project;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
class MasterApplicationTests {

	@Autowired
	private MainController mainController;
	@Test
	void testDockerfileCreation() {
		Project project = new Project();
		project.setLink("https://github.com/freemium-hosting/master-app.git");
		mainController.createProject(project);
	}

}
