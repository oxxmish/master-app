package ru.freemiumhosting.master.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import ru.freemiumhosting.master.model.dto.EditUserDto;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.utils.converters.StringDateConverter;

import java.time.OffsetDateTime;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "createdDate", target = "createdDate",
            qualifiedByName = "fromDate")
    @Mapping(target = "password", ignore = true)
    UserDto userToUserDto(User user);
    @Mapping(source = "createdDate", target = "createdDate",
            qualifiedByName = "fromString")
    User userDtoToUser(UserDto userDto);
    User editUserDtoToUser(EditUserDto userDto);


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
