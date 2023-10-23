package ru.bukhtaev.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ситуации, когда задано некорректное значение свойства.
 */
public class InvalidPropertyException extends CommonClientSideException {

    /**
     * Конструктор.
     *
     * @param errorMessage сообщение об ошибке
     * @param paramNames   названия параметров, значения которых привели к исключению
     */
    public InvalidPropertyException(final String errorMessage, final String... paramNames) {
        super(HttpStatus.BAD_REQUEST, errorMessage, paramNames);
    }
}
