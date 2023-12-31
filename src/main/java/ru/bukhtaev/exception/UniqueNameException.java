package ru.bukhtaev.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ситуации, когда нарушается уникальность имени сущности.
 */
public class UniqueNameException extends CommonClientSideException {

    /**
     * Конструктор.
     *
     * @param errorMessage сообщение об ошибке
     * @param paramNames   названия параметров, значения которых привели к исключению
     */
    public UniqueNameException(final String errorMessage, final String... paramNames) {
        super(HttpStatus.BAD_REQUEST, errorMessage, paramNames);
    }
}
