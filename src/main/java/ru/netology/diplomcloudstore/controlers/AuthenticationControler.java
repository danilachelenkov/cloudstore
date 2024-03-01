package ru.netology.diplomcloudstore.controlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import ru.netology.diplomcloudstore.domain.LoginResponse;
import ru.netology.diplomcloudstore.dto.LoginUserDto;
import ru.netology.diplomcloudstore.entities.User;
import ru.netology.diplomcloudstore.services.AuthenticationService;
import ru.netology.diplomcloudstore.services.JwtService;

@RequiredArgsConstructor
@RestController
public class AuthenticationControler {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    //private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/cloud/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto loginUserDto) {

        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        return new ResponseEntity<>(new LoginResponse(jwtToken), HttpStatus.OK);
    }

    @PostMapping("/cloud/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("@PostMapping(\"/cloud/logout\")");

        //todo подумать как удалить запись jwt и где она проверяется
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            User currentUser = (User) auth.getPrincipal();
            jwtService.deleteJwtFromDB(currentUser.getUsername());

            SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
            securityContextLogoutHandler.setInvalidateHttpSession(true);
            securityContextLogoutHandler.setClearAuthentication(true);
            securityContextLogoutHandler.logout(request, response, auth);
        }

       // if (invalidateHttpSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                //logger.debug("Invalidating session: " + session.getId());
                // # 1. Сделать текущий сеанс недействительным
                session.invalidate();
            }
    //    }

        //if (clearAuthentication) {
            SecurityContext context = SecurityContextHolder.getContext();
            // # 2. Очистить текущий `SecurityContext`
            context.setAuthentication(null);
        //}

        SecurityContextHolder.clearContext();

        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }
}
