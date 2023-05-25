package ru.freemiumhosting.master.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.dto.EditUserDto;
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
        checkUniqueName(userDto.getName());
        User user = userMapper.userDtoToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUserRole(UserRole.USER);
        userRep.save(user);
    }


    public void deleteUser(Long id) {
        //TODO delete users projects
        checkUserWithIdExist(id);
        checkForAdmin(id);
        userRep.deleteById(id);
    }

    public UserDto editUser(EditUserDto userDto) {
        checkUserWithIdExist(userDto.getId());
        checkUniqueName(userDto.getName());
        User user = userMapper.editUserDtoToUser(userDto);
        userRep.save(user);
        return userMapper.userToUserDto(user);
    }

    private void checkForAdmin(Long id) {
        if (userRep.findById(id).get().getUserRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Нельзя удалить администратора");
        }
    }

    private void checkUserWithIdExist(Long id) {
        if (userRep.findById(id).isEmpty()) {
            throw new EntityNotFoundException(String.format("Пользователь с id %s не найден", id));
        }
        ;
    }

    private void checkUniqueName(String name) {
        if (userRep.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalStateException(String.format("Пользователь с именем %s занят", name));
        }
    }


}
