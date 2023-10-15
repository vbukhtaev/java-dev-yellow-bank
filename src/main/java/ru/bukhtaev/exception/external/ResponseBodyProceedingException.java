package ru.bukhtaev.exception.external;

import ru.bukhtaev.exception.CommonServerSideException;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда не удалось преобразовать тело ответа в объект.
 */
public class ResponseBodyProceedingException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param responseBody тело ответа
     */
    public ResponseBodyProceedingException(final String responseBody) {
        super(MessageFormat.format("Failed to proceed response body: <{0}>", responseBody));
    }
}
