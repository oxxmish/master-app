package ru.freemiumhosting.master.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRs {
    @JsonProperty("authorization_token")
    private String authToken;
    @JsonProperty("role")
    private String role;
}
