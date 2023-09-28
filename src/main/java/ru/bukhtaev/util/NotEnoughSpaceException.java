package ru.bukhtaev.util;

/**
 * Исключение для ситуации, когда превышается вместимость хранилища.
 */
public class NotEnoughSpaceException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param message сообщение
     */
    public NotEnoughSpaceException(String message) {
        super(message);
    }
}
