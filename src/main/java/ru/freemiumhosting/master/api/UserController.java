package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.freemiumhosting.master.model.dto.UserDto;
import ru.freemiumhosting.master.service.UserService;
import ru.freemiumhosting.master.utils.enums.UserRole;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Secured(value = "ADMIN")
public class UserController {
    private final UserService userService;

    @GetMapping
    List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    void createUser(@RequestBody UserDto userDto) {
        userService.createUser(userDto);
    }

    @PutMapping("/{id}")
    UserDto editUser(@RequestBody UserDto userDto, @PathVariable Long id) {
        userDto.setId(id);
        return userService.editUser(userDto);
    }

    @DeleteMapping("/{id}")
    void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
