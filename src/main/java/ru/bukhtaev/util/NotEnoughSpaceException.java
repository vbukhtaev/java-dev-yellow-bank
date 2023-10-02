package ru.bukhtaev.util;

import java.text.MessageFormat;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_NOT_ENOUGH_STORAGE_SPACE;

/**
 * Исключение для ситуации, когда превышается вместимость хранилища.
 */
public class NotEnoughSpaceException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param freeSpace свободное пространство в хранилище
     */
    public NotEnoughSpaceException(final int freeSpace) {
        super(
                MessageFormat.format(MESSAGE_NOT_ENOUGH_STORAGE_SPACE, freeSpace)
        );
    }
}
