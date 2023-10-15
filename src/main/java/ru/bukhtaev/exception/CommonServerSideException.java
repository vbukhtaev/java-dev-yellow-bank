package ru.bukhtaev.exception;

import org.springframework.http.HttpStatus;

/**
 * Общее исключение для ситуации, когда возникла ошибка по вине сервера.
 * В данном случае под сервером подразумевается это приложение.
 */
public class CommonServerSideException extends CommonException {

    /**
     * Конструктор.
     *
     * @param errorMessage сообщение об ошибке
     */
    public CommonServerSideException(final String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
