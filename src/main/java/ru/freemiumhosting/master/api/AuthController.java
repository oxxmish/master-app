package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import ru.freemiumhosting.master.security.AuthRs;

import java.util.ArrayList;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth-data")
public class AuthController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    AuthRs getAuthority() {
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        ArrayList<? extends GrantedAuthority> grantedAuthorities = new ArrayList<>(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities());
        return new AuthRs(sessionId, grantedAuthorities.get(0).toString());
    }
}
