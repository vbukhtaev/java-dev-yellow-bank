package ru.bukhtaev.exception;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда нет доступа к внешнему API для указанного токена.
 */
public class AccessDeniedException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param tokenParamName название параметра для передачи токена
     */
    public AccessDeniedException(final String tokenParamName) {
        super(MessageFormat.format(
                "API token provided in <{0}> parameter does not have access to the resource!",
                tokenParamName
        ));
    }
}
