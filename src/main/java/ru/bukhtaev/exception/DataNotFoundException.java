package ru.bukhtaev.exception;

/**
 * Исключение для ситуации, когда данные не найдены.
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
