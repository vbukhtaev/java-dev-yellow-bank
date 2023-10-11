package ru.bukhtaev.exception;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда в запросе не предоставлен токен для взаимодействия с внешним API.
 */
public class TokenNotProvidedException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param tokenParamName название параметра для передачи токена
     */
    public TokenNotProvidedException(final String tokenParamName) {
        super(MessageFormat.format(
                "API token was not provided in <{0}> parameter!",
                tokenParamName
        ));
    }
}
