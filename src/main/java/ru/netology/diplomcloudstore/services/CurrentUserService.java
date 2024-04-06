package ru.netology.diplomcloudstore.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public UserEntity getCurrentUser() throws UsernameNotFoundException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<UserEntity> user = userRepository.findByUsername(userDetails.getUsername());

        if (user.isPresent()) {
            return user.get();
        } else {
            String msg = "CurrentUserService.getCurrentUser: The user '%s' not found in database";
            log.error(String.format(msg, userDetails.getUsername()));
            throw new UsernameNotFoundException(
                    String.format(msg,
                            userDetails.getUsername()
                    )
            );
        }
    }
}
