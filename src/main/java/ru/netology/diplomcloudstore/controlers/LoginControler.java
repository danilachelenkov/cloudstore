package ru.netology.diplomcloudstore.controlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.diplomcloudstore.domain.LoginResponse;
import ru.netology.diplomcloudstore.dto.LoginUserDto;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.services.AuthenticationService;
import ru.netology.diplomcloudstore.services.JwtService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cloud")
public class LoginControler {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto loginUserDto) {
        UserEntity authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        return new ResponseEntity<>(new LoginResponse(jwtToken), HttpStatus.OK);
    }
}
