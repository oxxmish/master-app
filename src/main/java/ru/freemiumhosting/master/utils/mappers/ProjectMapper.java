package ru.freemiumhosting.master.utils.mappers;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.utils.converters.StringDateConverter;

import java.time.OffsetDateTime;

@Mapper
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "fromDate")
    ProjectDto projectToProjectDto(Project project);

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(source = "createdDate", target = "createdDate",
            qualifiedByName = "fromString",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Project projectDtoToProject(ProjectDto projectDto);

    @Named("fromString")
    static OffsetDateTime fromString(String date) {
        if (date == null) return OffsetDateTime.now();
        return StringDateConverter.fromString(date);
    }

    @Named("fromDate")
    static String fromDate(OffsetDateTime date) {
        return StringDateConverter.fromDate(date);
    }


}
