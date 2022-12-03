package ru.freemiumhosting.master.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.freemiumhosting.master.model.ProjectStatus;

@Data
public class ProjectDto {
    private String name;
    private String link;
    private String branch;
    private ProjectStatus status = ProjectStatus.UNDEFINED;
    private String language;
    private String lastLaunch = "true";
    private String currentLaunch = "true";
    private List<String> envNames;
    private List<String> envValues;

    public Map<String, String> getEnvs() {
        return IntStream
            .range(0, envNames.size())
            .mapToObj(i -> Pair.of(envNames.get(i), envValues.get(i)))
            .filter(p -> !StringUtils.isEmpty(p.getKey()))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
