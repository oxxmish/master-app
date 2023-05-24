package ru.freemiumhosting.master.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.utils.enums.UserRole;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public List<UserDto> getUsers() {
        List<User> users = userRep.findAll();
        return users.stream().map(userMapper::userToUserDto).collect(Collectors.toList());
    }

    public void createUser(UserDto userDto) {
        userRep.findByNameIgnoreCase(userDto.getName())
                .orElseThrow(() -> new IllegalStateException("Пользоавтель с таким именем уже занят"));
        User user = userMapper.userDtoToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUserRole(UserRole.USER);
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
