package ru.freemiumhosting.master.utils.mappers;

import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.model.dto.UserDto;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    ProjectDto projectToProjectDto(Project project);

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    Project projectDtoToProject(ProjectDto projectDto);
}
