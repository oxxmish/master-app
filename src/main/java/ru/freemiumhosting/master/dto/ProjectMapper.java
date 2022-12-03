package ru.freemiumhosting.master.dto;

import org.mapstruct.Mapper;
import ru.freemiumhosting.master.model.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectDto toDto(Project project);
    Project toEntity(ProjectDto project);
}
