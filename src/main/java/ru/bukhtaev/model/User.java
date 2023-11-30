package ru.bukhtaev.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.bukhtaev.security.Role;

import java.util.Objects;
import java.util.UUID;

/**
 * Модель пользователя.
 */
@Getter
@Setter
@Entity
@Table(name = "user_table")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    /**
     * Название поля, хранящего логин.
     */
    public static final String FIELD_USERNAME = "username";

    /**
     * Логин.
     */
    @NotBlank
    @Column(name = "username", length = 64, nullable = false, unique = true)
    private String username;

    /**
     * Пароль.
     */
    @NotBlank
    @Column(name = "password", length = 256, nullable = false)
    private String password;

    /**
     * Роль.
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role", length = 32, nullable = false)
    private Role role;

    /**
     * Конструктор.
     *
     * @param id       ID
     * @param username логин
     * @param password пароль
     * @param role     роль
     */
    @Builder
    public User(
            final UUID id,
            final String username,
            final String password,
            final Role role
    ) {
        super(id);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}

