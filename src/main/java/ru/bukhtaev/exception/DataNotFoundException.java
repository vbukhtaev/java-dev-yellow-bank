package ru.bukhtaev.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ситуации, когда не удалось найти запрашиваемые данные.
 */
public class DataNotFoundException extends CommonClientSideException {

    /**
     * Конструктор.
     *
     * @param errorMessage сообщение об ошибке
     * @param paramNames   названия параметров, значения которых привели к исключению
     */
    public DataNotFoundException(final String errorMessage, final String... paramNames) {
        super(HttpStatus.NOT_FOUND, errorMessage, paramNames);
    }
}
