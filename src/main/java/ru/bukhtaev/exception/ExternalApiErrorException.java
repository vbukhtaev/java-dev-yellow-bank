package ru.bukhtaev.exception;

/**
 * Исключение для ситуации, когда произошла ошибка на сервере внешнего API.
 */
public class ExternalApiErrorException extends CommonServerSideException {

    /**
     * Конструктор.
     */
    public ExternalApiErrorException() {
        super("External API error occurred!");
    }
}
