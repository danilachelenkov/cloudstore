package ru.netology.diplomcloudstore.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.dto.LoginUserDto;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.repositories.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserEntity authenticate(LoginUserDto loginUserDto) throws UsernameNotFoundException, BadCredentialsException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginUserDto.getUsername(),
                            loginUserDto.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            String msg = "AuthenticationService.authenticate: User can not authenticated. " + e.getMessage();
            log.error(msg);
            throw new UsernameNotFoundException(msg);
        }

        return userRepository.findByUsername(loginUserDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("AuthenticationService.authenticate: User %s not found in database", loginUserDto.getUsername())));
    }
}
