package ru.bukhtaev.validation.handling;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Ответ на ошибку.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Ответ на ошибку")
public final class ErrorResponse {

    /**
     * Список нарушений.
     */
    @Schema(description = "Список нарушений")
    private final List<Violation> violations;

    /**
     * Дата и время ошибки.
     */
    @Schema(description = "Дата и время ошибки")
    private final LocalDateTime timestamp;

    /**
     * Конструктор для случая, когда нарушение одно.
     *
     * @param violation нарушение
     * @param timestamp дата и время ошибки
     */
    public ErrorResponse(final Violation violation, final LocalDateTime timestamp) {
        this.violations = Collections.singletonList(violation);
        this.timestamp = timestamp;
    }
}
