package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.exception.GitCloneException;
import ru.freemiumhosting.master.model.Env;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.EnvRep;
import ru.freemiumhosting.master.repository.ProjectRep;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvService {
    private final EnvRep envRep;

    public void createEnvs(List<String> envKeys, List<String> envValues, Project project) {
        for (int i = 0; i < envKeys.size(); i++) {
            envRep.save(new Env(envKeys.get(i), envValues.get(i), project));
        }
    }

    public void updateEnvs(List<String> envKeys, List<String> envValues, Project project) {
        envRep.deleteAllByProject(project);
        for (int i = 0; i < envKeys.size(); i++) {
            envRep.save(new Env(envKeys.get(i), envValues.get(i), project));
        }
    }

    public Map<String, String> getEnvsByProject(Project project) {
        List<Env> envs = envRep.findAllByProject(project);
        return IntStream
                .range(0, envs.size())
                .mapToObj(i -> Pair.of(envs.get(i).getEnv_key(), envs.get(i).getEnv_value()))
                .filter(p -> !StringUtils.isEmpty(p.getKey()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
