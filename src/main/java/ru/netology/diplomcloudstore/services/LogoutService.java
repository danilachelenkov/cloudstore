package ru.netology.diplomcloudstore.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.repositories.UserJwtRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final UserJwtRepository userJwtRepository;
    @Transactional
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Auth-Token");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String jwtToken = authHeader.substring(7);

        log.debug(String.format("LogoutService.logout: Start checking jwtToken '%s' in database blacklist...",jwtToken));

        var storedJwtToken = userJwtRepository.findByJwt(jwtToken)
                .orElse(null);

        if (storedJwtToken != null) {
            storedJwtToken.setExpired(true);
            storedJwtToken.setRevoke(true);
            userJwtRepository.save(storedJwtToken);
            log.debug(String.format("LogoutService.logout: Jwt token '%s' was set as revoked and expired",jwtToken));
        }else {
            log.debug(String.format("LogoutService.logout: Jwt token '%s' not found in database blacklist table",jwtToken));
        }
    }
}
