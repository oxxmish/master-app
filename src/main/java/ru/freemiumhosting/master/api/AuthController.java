package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import ru.freemiumhosting.master.security.AuthRs;
import ru.freemiumhosting.master.utils.enums.UserRole;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth-data")
public class AuthController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    AuthRs getAuthority() {
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        return new AuthRs(sessionId, UserRole.USER.name());
    }
}
