package ru.bukhtaev.util.external;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда превышен месячный лимит запросов к внешнему API для указанного токена.
 */
public class TokenLimitExceededException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param tokenParamName название параметра для передачи токена
     */
    public TokenLimitExceededException(final String tokenParamName) {
        super(MessageFormat.format(
                "Exceeded calls per month limit for the token provided in <{0}> parameter!",
                tokenParamName
        ));
    }
}
