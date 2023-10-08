package ru.bukhtaev.util.external;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда запрос к внешнему API содержит некорректный URL.
 */
public class InvalidUrlException extends CommonServerSideException {

    /**
     * Конструктор.
     *
     * @param url URL
     */
    public InvalidUrlException(final String url) {
        super(MessageFormat.format(
                "Invalid request url: <{0}>",
                url
        ));
    }
}
