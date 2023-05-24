package ru.freemiumhosting.master.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.freemiumhosting.master.utils.enums.UserRole;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String name;
    private String password;
    private String createdDate;
    private UserRole userRole;
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
