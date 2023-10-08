package ru.bukhtaev.util.external;

import org.springframework.http.HttpStatus;

/**
 * Общее исключение для ситуации, когда возникла ошибка по вине клиента.
 * В данном случае под клиентом подразумевается пользователь этого приложения.
 */
public class CommonServerSideException extends ExternalApiException {

    /**
     * Конструктор.
     *
     * @param errorMessage сообщение об ошибке
     */
    public CommonServerSideException(final String errorMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
