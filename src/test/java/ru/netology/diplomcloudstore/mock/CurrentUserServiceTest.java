package ru.netology.diplomcloudstore.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.repositories.UserRepository;
import ru.netology.diplomcloudstore.services.CurrentUserService;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrentUserServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    CurrentUserService currentUserService;

    @Test
    public void getCurrentUser_ReturnException() {

        //given
        UserEntity userEntity = new UserEntity();
        userEntity.setId(25L);
        userEntity.setFullName("123");
        userEntity.setPassword("321");
        userEntity.setUsername("user1@gmail.com");

        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);

        System.out.println("Check not throw Exception, but userEntity NOT null");
        when(this.userRepository.findByUsername(userDetails.getUsername())).thenReturn(Optional.of(userEntity));
        Assertions.assertDoesNotThrow(() -> currentUserService.getCurrentUser());

        System.out.println("Check throw Exception, but userEntity IS null");
        when(this.userRepository.findByUsername(userDetails.getUsername())).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            currentUserService.getCurrentUser();
        });
    }
}