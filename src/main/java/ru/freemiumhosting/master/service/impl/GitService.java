package ru.freemiumhosting.master.service.impl;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {
    @Value("${freemium.hosting.git-clone-path}")
    private String gitClonePath;

    public void cloneGitRepo(String uri) {
        try {
            Git git = Git.cloneRepository()
                    .setURI(uri)
                    .setDirectory(new File(gitClonePath))
                    .call();
        } catch (GitAPIException ex) {
            System.err.println("Возникла проблема при клонировании git репозитория");
        }
    }
}
