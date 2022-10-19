package ru.freemiumhosting.master.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.freemiumhosting.master.service.impl.GitService;

@TestConfiguration
public class GitServiceTestConfiguration {

    @Bean
    public GitService gitService() {
        return new GitService();
    }
}
