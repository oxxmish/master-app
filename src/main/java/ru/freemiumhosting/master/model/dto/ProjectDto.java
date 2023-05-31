package ru.freemiumhosting.master.model.dto;

import java.time.OffsetDateTime;
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
    private Long id;
    private String name;
    private String description;
    private String type;
    private String gitUrl;
    private String gitBranch;
    private ProjectStatus status;
    private Long ownerId;
    private String ownerName;
    private List<String> ports;
    private List<String> envs;
    private String createdDate;
    private Double cpuConsumption;
    private Double cpuRequest;
    private Double ramConsumption;
    private Double ramRequest;
    private Double storageConsumption;
    private Double storageRequest;
    private String appLink;
}
