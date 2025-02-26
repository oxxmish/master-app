package ru.freemiumhosting.master.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ru.freemiumhosting.master.model.User;
import ru.freemiumhosting.master.utils.enums.UserRole;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter
public class SecurityUser implements UserDetails {
    private Long userId;
    private String username;
    private String password;
    private UserRole role;

    public static SecurityUser getCurrentUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static SecurityUser fromUser(User user)  {
        return new SecurityUser(user.getId(), user.getName(), user.getPassword(), user.getUserRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
