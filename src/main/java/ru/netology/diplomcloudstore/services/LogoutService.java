package ru.netology.diplomcloudstore.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import ru.netology.diplomcloudstore.repositories.UserJwtRepository;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final UserJwtRepository userJwtRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String jwt = authHeader.substring(7);
        var storedJwtToken = userJwtRepository.findByJwt(jwt)
                .orElse(null);

        if (storedJwtToken != null) {
            storedJwtToken.setExpired(true);
            storedJwtToken.setRevoke(true);
            userJwtRepository.save(storedJwtToken);
        }

    }
}
