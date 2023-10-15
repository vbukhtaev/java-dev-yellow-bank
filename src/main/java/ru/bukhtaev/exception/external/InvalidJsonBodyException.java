package ru.bukhtaev.exception.external;

import ru.bukhtaev.exception.CommonServerSideException;

/**
 * Исключение для ситуации, когда передано некорректное тело массового запроса.
 */
public class InvalidJsonBodyException extends CommonServerSideException {

    /**
     * Конструктор.
     */
    public InvalidJsonBodyException() {
        super("Json body passed in bulk request is invalid!");
    }
}
