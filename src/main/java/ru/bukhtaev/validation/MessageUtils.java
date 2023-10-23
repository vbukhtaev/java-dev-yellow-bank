package ru.bukhtaev.validation;

import ru.bukhtaev.service.GenerationServiceImpl;
import ru.bukhtaev.service.WeatherProcessingServiceImpl;
import ru.bukhtaev.service.crud.WeatherCrudServiceJpaImpl;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Утилитный класс со статическими константами, содержащими коды сообщений и сообщения.
 */
public final class MessageUtils {

    /**
     * Только для статического использования.
     */
    private MessageUtils() {
    }

    /**
     * Код сообщения о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передано некорректное количество записей.
     */
    public static final String MESSAGE_CODE_THERE_IS_NO_DATA = "validation.there-is-no-weather-data";

    /**
     * Код сообщения о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передан пустой список городов.
     */
    public static final String MESSAGE_CODE_EMPTY_CITIES_LIST = "validation.empty-cities-set";

    /**
     * Код сообщения о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передан пустой список типов погоды.
     */
    public static final String MESSAGE_CODE_EMPTY_TYPES_LIST = "validation.empty-types-set";

    /**
     * Код сообщения о том, что методу {@link WeatherProcessingServiceImpl#getAverageTemperature(List, int)}
     * не удалось вычислить общую среднюю температуру.
     */
    public static final String MESSAGE_CODE_INCORRECT_COUNT = "validation.invalid-count";

    /**
     * Код сообщения о том, что не удалось вычислить среднюю температуру.
     */
    public static final String MESSAGE_CODE_FAILED_TO_COMPUTE = "validation.failed-to-compute";

    /**
     * Код сообщения о том, что методу {@link WeatherCrudServiceJpaImpl#getTemperature(String, ChronoUnit)}
     * не удалось найти запрашиваемые данные.
     */
    public static final String MESSAGE_CODE_TEMPERATURE_NOT_FOUND = "validation.temperature-not-found";

    /**
     * Код сообщения о том, что город с указанным ID не найден.
     */
    public static final String MESSAGE_CODE_CITY_NOT_FOUND = "validation.city.not-found";

    /**
     * Код сообщения о том, что город с указанным названием уже существует.
     */
    public static final String MESSAGE_CODE_CITY_UNIQUE_NAME = "validation.city.unique-name";

    /**
     * Код сообщения о том, что тип погоды с указанным ID не найден.
     */
    public static final String MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND = "validation.weather-type.not-found";

    /**
     * Код сообщения о том, что тип погоды с указанным названием уже существует.
     */
    public static final String MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME = "validation.weather-type.unique-name";

    /**
     * Код сообщения о том, что запись о погоде с указанным ID не найдена.
     */
    public static final String MESSAGE_CODE_WEATHER_NOT_FOUND = "validation.weather.not-found";

    /**
     * Код сообщения о том, что задано некорректное значение свойства.
     */
    public static final String MESSAGE_CODE_INVALID_FIELD = "validation.common.invalid-field";

    /**
     * Код сообщения о том, что запись о погоде с указанным городом, датой и временем уже существует.
     */
    public static final String MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME
            = "validation.weather-type.unique-city-and-time";
}
