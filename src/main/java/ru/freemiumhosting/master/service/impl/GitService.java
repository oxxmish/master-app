package ru.freemiumhosting.master.service.impl;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {
    public void cloneGitRepo(String gitClonePath, String uri, String branch) {
        try (Git git = Git.cloneRepository()
            .setURI(uri)
            .setDirectory(new File(gitClonePath)) //TODO: clean folder
            .setBranch(branch)
            .call()) {
        } catch (GitAPIException ex) {
            System.err.println("Возникла проблема при клонировании git репозитория");
            ex.printStackTrace(); //TODO: show error on frontend
        }
    }
}
