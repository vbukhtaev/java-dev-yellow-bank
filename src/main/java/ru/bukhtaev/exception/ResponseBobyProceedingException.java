package ru.bukhtaev.exception;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда не удалось преобразовать тело ответа в объект.
 */
public class ResponseBobyProceedingException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param responseBody тело ответа
     */
    public ResponseBobyProceedingException(final String responseBody) {
        super(MessageFormat.format("Failed to proceed response body: <{0}>", responseBody));
    }
}
