package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import ru.bukhtaev.dto.UserRequestDto;
import ru.bukhtaev.repository.jpa.IUserJpaRepository;
import ru.bukhtaev.security.Role;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.controller.RegistrationController.URL_SIGN_UP;

/**
 * Интеграционные тесты регистрации пользователей.
 */
class RegistrationControllerIT extends AbstractIntegrationTest {

    /**
     * Репозиторий.
     */
    @Autowired
    private IUserJpaRepository repository;

    private UserRequestDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserRequestDto.builder()
                .username("user")
                .password("password")
                .build();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser
    void register_withAuthentication_accessShouldBeDenied() throws Exception {
        // given
        assertThat(repository.findAll()).isEmpty();
        final String jsonRequest = objectMapper.writeValueAsString(userDto);
        final var requestBuilder = post(URL_SIGN_UP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @WithAnonymousUser
    void register_withoutAuthenticationAndNonExistentUsername_shouldRegisterUser() throws Exception {
        // given
        assertThat(repository.findAll()).isEmpty();
        final String jsonRequest = objectMapper.writeValueAsString(userDto);
        final var requestBuilder = post(URL_SIGN_UP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().string(MessageFormat.format(
                                "User with login <{0}> has been successfully registered!",
                                userDto.getUsername()
                        ))
                );

        final var users = repository.findAll();
        assertThat(users).hasSize(1);
        final var user = users.get(0);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo(userDto.getUsername());
        assertThat(user.getPassword()).isNotNull();
        assertThat(user.getPassword()).isNotEqualTo(userDto.getPassword());
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @WithAnonymousUser
    void register_withoutAuthenticationAndExistentUsername_shouldNotRegisterUser() throws Exception {
        // given
        final String jsonRequest = objectMapper.writeValueAsString(userDto);
        final var requestBuilder = post(URL_SIGN_UP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().string(MessageFormat.format(
                                "User with login <{0}> has been successfully registered!",
                                userDto.getUsername()
                        ))
                );
        final var registeredUsers = repository.findAll();
        assertThat(registeredUsers).hasSize(1);
        final var registered = registeredUsers.get(0);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(MessageFormat.format(
                                "User with username <{0}> already registered!",
                                userDto.getUsername()
                        )))
                );

        final var users = repository.findAll();
        assertThat(users).hasSize(1);
        final var user = users.get(0);
        assertThat(user.getId()).isEqualTo(registered.getId());
        assertThat(user.getUsername()).isEqualTo(registered.getUsername());
        assertThat(user.getPassword()).isEqualTo(registered.getPassword());
        assertThat(user.getRole()).isEqualTo(registered.getRole());
    }
}