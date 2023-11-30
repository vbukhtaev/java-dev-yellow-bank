package ru.bukhtaev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.bukhtaev.model.User;

/**
 * DTO для модели {@link User}, используемый в качестве тела HTTP-запроса.
 */
@Schema(description = "Пользователь")
@Getter
@Builder
@AllArgsConstructor
public class UserRequestDto {

    /**
     * Логин.
     */
    @Schema(description = "Логин")
    @NotBlank
    private String username;

    /**
     * Пароль.
     */
    @Schema(description = "Пароль")
    @NotBlank
    private String password;
}
