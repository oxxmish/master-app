package ru.freemiumhosting.master.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.repository.UserRep;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRep userRep;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRep.findByNameIgnoreCase(username).orElseThrow();
        return SecurityUser.fromUser(user);
    }
}