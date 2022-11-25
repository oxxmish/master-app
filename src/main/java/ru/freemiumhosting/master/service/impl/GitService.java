package ru.freemiumhosting.master.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
@Slf4j
public class GitService {
    @Value("${freemium.hosting.git-clone-path}")
    private String gitClonePath;

    public void cloneGitRepo(String uri, String branch, Long projectId) {
        try {
            log.info("Старт клонирования репозитория " + uri);
            Git.cloneRepository()
                    .setURI(uri)
                    .setDirectory(new File(gitClonePath))
                    .call();
            log.info("Клонирование репозитория " + uri + " закончено");
        } catch (GitAPIException ex) {
            System.err.println("Возникла проблема при клонировании git репозитория");
        }
    }
}
