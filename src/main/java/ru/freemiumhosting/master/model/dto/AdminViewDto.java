package ru.freemiumhosting.master.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminViewDto {
    private ClusterStatisticsDto clusterStatistics;
    private List<ProjectDto> projects;
}
