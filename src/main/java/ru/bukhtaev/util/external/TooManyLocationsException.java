package ru.bukhtaev.util.external;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда в теле массового запроса содержится слишком много местоположений.
 */
public class TooManyLocationsException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param limit лимит местоположений в теле массового запроса
     */
    public TooManyLocationsException(final int limit) {
        super(MessageFormat.format(
                "Json body contains more than <{0}> locations for bulk request!",
                limit
        ));
    }
}
