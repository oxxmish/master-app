package ru.freemiumhosting.master;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.freemiumhosting.master.utils.exception.GitCloneException;
import ru.freemiumhosting.master.service.impl.GitService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "local")
public class GitServiceTest {

    @Autowired
    private GitService gitService;
    @Value("${freemium.hosting.git-clone-path}")
    private String gitClonePath;

    @Test
    public void gitCloneTest() throws GitCloneException {
        gitService.cloneGitRepo(gitClonePath, "https://github.com/freemium-hosting/master-app.git", "master");
    }
}
