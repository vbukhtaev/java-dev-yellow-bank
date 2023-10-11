package ru.bukhtaev.exception;

import java.text.MessageFormat;

/**
 * Исключение для ситуации, когда клиент не предоставил местоположения.
 */
public class LocationNotProvidedException extends CommonClientSideException {

    /**
     * Конструктор.
     *
     * @param locationParamName название параметра для передачи местоположения
     */
    public LocationNotProvidedException(final String locationParamName) {
        super(
                MessageFormat.format(
                        "Necessary parameter <{0}> not provided!",
                        locationParamName
                ),
                locationParamName
        );
    }
}
