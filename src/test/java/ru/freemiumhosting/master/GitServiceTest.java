package ru.freemiumhosting.master;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.service.impl.GitService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class GitServiceTest {

    @Autowired
    private GitService gitService;

    @Test
    public void gitCloneTest() {
        gitService.cloneGitRepo("https://github.com/freemium-hosting/master-app.git", "master", 1L);
    }
}
