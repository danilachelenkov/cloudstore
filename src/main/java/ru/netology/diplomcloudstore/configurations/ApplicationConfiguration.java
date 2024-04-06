package ru.netology.diplomcloudstore.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.netology.diplomcloudstore.domain.ProgramSettings;
import ru.netology.diplomcloudstore.exceptions.NotFoundSettingInDatabaseException;
import ru.netology.diplomcloudstore.repositories.SettingRepository;
import ru.netology.diplomcloudstore.repositories.UserRepository;
import ru.netology.diplomcloudstore.services.CurrentUserService;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationConfiguration {
    private final UserRepository userRepository;
    private final SettingRepository settingRepository;

    @Bean
    UserDetailsService userDetailsService() throws UsernameNotFoundException {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in database. Check the registration user in database"));
    }

    @Bean
    public ProgramSettings programSettings() throws NotFoundSettingInDatabaseException {
        return new ProgramSettings(settingRepository);
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public CurrentUserService currentUserService(UserRepository userRepository) {
        return new CurrentUserService(userRepository);
    }

}
