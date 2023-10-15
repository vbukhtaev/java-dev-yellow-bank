package ru.bukhtaev.exception.external;

import org.springframework.http.HttpStatus;
import ru.bukhtaev.exception.CommonClientSideException;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда не удалось найти местоположение по значению, которое предоставил клиент.
 */
public class LocationNotFoundException extends CommonClientSideException {

    /**
     * Конструктор
     *
     * @param locationParamName название параметра для передачи местоположения
     * @param location          местоположение
     */
    public LocationNotFoundException(final String locationParamName, final String location) {
        super(HttpStatus.NOT_FOUND,
                MessageFormat.format(
                        "No location found for value <{0}>",
                        location
                ),
                locationParamName
        );
    }
}
