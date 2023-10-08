package ru.bukhtaev.util.external;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Общее исключение для ситуации, когда не удалось получить данные о погоде от внешнего API.
 */
@Getter
public abstract class ExternalApiException extends RuntimeException {

    /**
     * HTTP статус.
     */
    private final HttpStatus targetStatus;

    /**
     * Сообщение об ошибке.
     */
    private final String errorMessage;

    /**
     * Конструктор.
     *
     * @param targetStatus HTTP статус
     * @param errorMessage сообщение об ошибке
     */
    protected ExternalApiException(HttpStatus targetStatus, String errorMessage) {
        this.targetStatus = targetStatus;
        this.errorMessage = errorMessage;
    }
}
