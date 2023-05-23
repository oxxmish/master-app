package ru.freemiumhosting.master.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.utils.mappers.UserMapper;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.repository.UserRep;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRep userRep;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    public List<UserDto> getUsers() {
        List<User> users = userRep.findAll();
        return users.stream().map(userMapper::userToUserDto).collect(Collectors.toList());
    }

    public void createUser(UserDto userDto) {
        User user = userMapper.userDtoToUser(userDto);
        userRep.save(user);
    }


    public void deleteUser(Long id) {
        userRep.deleteById(id);
    }

    public UserDto editUser(UserDto userDto) {
        boolean isUserExist = userRep.findById(userDto.getId()).isPresent();
        if (isUserExist) {
            User user = userMapper.userDtoToUser(userDto);
            userRep.save(user);
            return userMapper.userToUserDto(user);
        } else {
            throw new EntityNotFoundException(String.format("Пользователь с id %s не найден", userDto.getId()));
        }
    }
}
