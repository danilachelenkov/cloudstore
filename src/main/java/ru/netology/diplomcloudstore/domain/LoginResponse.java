package ru.netology.diplomcloudstore.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    @JsonSetter("auth-token")
    private String token;
}
