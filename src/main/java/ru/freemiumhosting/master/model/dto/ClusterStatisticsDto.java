package ru.freemiumhosting.master.model.dto;

import lombok.Data;

@Data
public class ClusterStatisticsDto {
    private Double currentCpu;
    private Double availibleCpu = 2.0;
    private Double currentRam;
    private Double availibleRam = 2000.0;
    private Double currentStorage;
    private Double availibleStorage = 20.0;
}
