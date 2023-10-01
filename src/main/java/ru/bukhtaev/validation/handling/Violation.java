package ru.bukhtaev.validation.handling;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Нарушение валидации.
 */
@Schema(description = "Нарушение валидации")
@Getter
public class Violation {

    /**
     * Сообщение.
     */
    @Schema(description = "Сообщение")
    protected final String message;

    /**
     * Параметры, значения которых нарушают правила валидации.
     */
    @Schema(description = "Параметры, значения которых нарушают валидацию")
    protected final String[] paramNames;

    /**
     * Конструктор.
     *
     * @param message    сообщение
     * @param paramNames параметры, значения которых нарушают правила валидации
     */
    public Violation(final String message, final String... paramNames) {
        this.message = message;
        this.paramNames = paramNames;
    }
}
