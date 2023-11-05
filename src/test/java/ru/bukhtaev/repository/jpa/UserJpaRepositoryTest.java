package ru.bukhtaev.repository.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.User;
import ru.bukhtaev.security.Role;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест JPA-репозитория пользователей.
 */
@DataJpaTest
class UserJpaRepositoryTest extends AbstractContainerizedTest {

    /**
     * Тестируемый JPA-репозиторий пользователей.
     */
    @Autowired
    private IUserJpaRepository underTest;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .username("user1")
                .password("password1")
                .role(Role.USER)
                .build();
        user2 = User.builder()
                .username("user2")
                .password("password2")
                .role(Role.USER)
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByUsername_withExistentUsername_shouldReturnFoundEntity() {
        // given
        final User saved = underTest.save(user2);
        underTest.save(user1);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findByUsername(user2.getUsername());

        // then
        assertThat(optCity).isPresent();
        assertThat(optCity.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optCity.get().getUsername())
                .isEqualTo(user2.getUsername());
    }

    @Test
    void findByUsername_withNonExistentUsername_shouldReturnEmptyOptional() {
        // given
        final String anotherName = "another";
        underTest.save(user2);
        underTest.save(user1);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optCity = underTest.findByUsername(anotherName);

        // then
        assertThat(optCity).isNotPresent();
    }
}