package ru.netology.diplomcloudstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginUserDto {
    @JsonProperty("login")
    private String username;
    private String password;
}
