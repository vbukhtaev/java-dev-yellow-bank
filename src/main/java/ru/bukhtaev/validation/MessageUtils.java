package ru.bukhtaev.validation;

import ru.bukhtaev.repository.InMemoryRepository;
import ru.bukhtaev.service.GenerationServiceImpl;
import ru.bukhtaev.service.WeatherCrudServiceImpl;
import ru.bukhtaev.service.WeatherProcessingServiceImpl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

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
     * Код сообщения о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(Set, int)}
     * передано некорректное количество записей.
     */
    public static final String MESSAGE_CODE_THERE_IS_NO_DATA = "validation.there-is-no-weather-data";

    /**
     * Код сообщения о том, что в качестве аргумента в конструктор
     * {@link InMemoryRepository#InMemoryRepository(int)}
     * передана некорректная вместимость репозитория.
     */
    public static final String MESSAGE_CODE_EMPTY_CITIES_SET = "validation.empty-cities-set";

    /**
     * Код сообщения о том, что методу {@link WeatherProcessingServiceImpl#getAverageTemperature(List, int)}
     * не удалось вычислить общую среднюю температуру.
     */
    public static final String MESSAGE_CODE_INCORRECT_COUNT = "validation.invalid-count";

    /**
     * Код сообщения о том, что в репозитории {@link InMemoryRepository}
     * недостаточно свободного места.
     */
    public static final String MESSAGE_CODE_FAILED_TO_COMPUTE = "validation.failed-to-compute";

    /**
     * Код сообщения о том, что методу {@link WeatherCrudServiceImpl#getTemperature(String, ChronoUnit)}
     * не удалось найти запрашиваемые данные.
     */
    public static final String MESSAGE_CODE_TEMPERATURE_NOT_FOUND = "validation.temperature-not-found";

    /**
     * Сообщение о том, что в качестве аргумента в конструктор
     * {@link InMemoryRepository#InMemoryRepository(int)}
     * передана некорректная вместимость репозитория.
     */
    public static final String MESSAGE_INVALID_CAPACITY = "Invalid capacity: {0}";

    /**
     * Сообщение о том, что в репозитории {@link InMemoryRepository}
     * недостаточно свободного места.
     */
    public static final String MESSAGE_NOT_ENOUGH_STORAGE_SPACE
            = "Not enough storage space! Free storage space: {0}";
}
