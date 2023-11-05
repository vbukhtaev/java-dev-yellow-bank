package ru.bukhtaev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.bukhtaev.security.UserDetailsServiceImpl;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
import static ru.bukhtaev.security.Permission.H2_CONSOLE;

/**
 * Конфигурация Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Сервис предоставления данных о пользователе.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Конструктор.
     *
     * @param userDetailsService сервис предоставления данных о пользователе
     */
    @Autowired
    public SecurityConfig(final UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .httpBasic(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(this.userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(toH2Console()).hasAuthority(H2_CONSOLE.getDescriptor())
                        .requestMatchers(antMatcher(POST, "/sign-up")).anonymous()
                        .requestMatchers(
                                antMatcher("/error"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html"),
                                antMatcher("/v3/api-docs/**"))
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
