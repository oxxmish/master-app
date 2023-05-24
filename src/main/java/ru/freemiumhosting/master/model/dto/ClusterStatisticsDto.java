package ru.freemiumhosting.master.model.dto;

import lombok.Data;

@Data
public class ClusterStatisticsDto {
    private Double currentCpu = 2.0;
    private Double availibleCpu = 2.0;
    private Double currentRam = 2.0;
    private Double availibleRam = 2.0;
    private Double currentStorage = 2.0;
    private Double availibleStorage = 2.0;
}
