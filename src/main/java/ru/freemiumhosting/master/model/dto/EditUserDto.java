package ru.freemiumhosting.master.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditUserDto {
    private Long id;
    private String name;
    private Long currentCpu;
    private Long requestCpu;
    private Long availibleCpu;
    private Long currentRam;
    private Long requestRam;
    private Long availibleRam;
    private Long currentStorage;
    private Long requestStorage;
    private Long availibleStorage;
}
