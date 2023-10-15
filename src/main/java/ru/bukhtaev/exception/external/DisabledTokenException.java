package ru.bukhtaev.exception.external;

import ru.bukhtaev.exception.CommonServerSideException;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда переданный для взаимодействия с внешним API токен более не действителен.
 */
public class DisabledTokenException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param tokenParamName название параметра для передачи токена
     */
    public DisabledTokenException(final String tokenParamName) {
        super(MessageFormat.format(
                "API token provided in <{0}> parameter has been disabled!",
                tokenParamName
        ));
    }
}
