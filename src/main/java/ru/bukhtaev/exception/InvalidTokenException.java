package ru.bukhtaev.exception;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда предоставлен некорректный токен для взаимодействия с внешним API.
 */
public class InvalidTokenException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param tokenParamName название параметра для передачи токена
     */
    public InvalidTokenException(final String tokenParamName) {
        super(MessageFormat.format(
                "API token provided in <{0}> parameter is invalid!",
                tokenParamName
        ));
    }
}
