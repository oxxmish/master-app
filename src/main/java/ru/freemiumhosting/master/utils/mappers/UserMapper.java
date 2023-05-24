package ru.freemiumhosting.master.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.model.User;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    UserDto userToUserDto(User user);
    @Mapping(target = "createdDate", ignore = true)
    User userDtoToUser(UserDto userDto);
}
