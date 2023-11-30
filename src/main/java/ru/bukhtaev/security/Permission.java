package ru.bukhtaev.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Модель разрешения.
 */
@Getter
@RequiredArgsConstructor
public enum Permission {

    /**
     * Чтение городов.
     */
    CITIES_READ("cities:read"),

    /**
     * Изменение городов.
     */
    CITIES_WRITE("cities:write"),

    /**
     * Чтение типов погоды.
     */
    WEATHER_TYPES_READ("weather-types:read"),

    /**
     * Изменение типов погоды.
     */
    WEATHER_TYPES_WRITE("weather-types:write"),

    /**
     * Чтение данных о погоде.
     */
    WEATHER_DATA_READ("weather-data:read"),

    /**
     * Изменение данных о погоде.
     */
    WEATHER_DATA_WRITE("weather-data:write"),

    /**
     * Доступ к консоли базы данных H2.
     */
    H2_CONSOLE("h2:console");

    /**
     * Дескриптор роли.
     */
    private final String descriptor;
}
