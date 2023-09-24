package ru.bukhtaev.util;

/**
 * Исключение для ситуации, когда данных нет.
 */
public class DataNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param message сообщение об ошибке
     */
    public DataNotFoundException(final String message) {
        super(message);
    }
}
