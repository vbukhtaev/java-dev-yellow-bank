package ru.bukhtaev;

import ru.bukhtaev.repository.InMemoryRepository;
import ru.bukhtaev.service.GenerationServiceImpl;
import ru.bukhtaev.service.WeatherCrudServiceImpl;
import ru.bukhtaev.service.WeatherProcessingServiceImpl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Утилитный класс, содержащий полезные для тестирования константы.
 */
public class TestUtils {

    /**
     * Только для статического использования.
     */
    private TestUtils() {
    }

    /**
     * Сообщение о том, что отсутствуют данные о погоде.
     */
    public static final String MESSAGE_THERE_IS_NO_DATA = "There is no weather data!";

    /**
     * Сообщение о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(Set, int)}
     * передано пустое множество городов.
     */
    public static final String MESSAGE_EMPTY_CITIES_SET = "Empty cities set!";

    /**
     * Сообщение о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(Set, int)}
     * передано некорректное количество записей.
     */
    public static final String MESSAGE_INCORRECT_COUNT = "Invalid count: {0}";

    /**
     * Сообщение о том, что методу {@link WeatherProcessingServiceImpl#getAverageTemperature(List, int)}
     * не удалось вычислить общую среднюю температуру.
     */
    public static final String MESSAGE_FAILED_TO_COMPUTE = "Failed to compute!";

    /**
     * Сообщение о том, что методу {@link WeatherCrudServiceImpl#getTemperature(String, ChronoUnit)}
     * не удалось найти запрашиваемые данные.
     */
    public static final String MESSAGE_TEMPERATURE_NOT_FOUND
            = "No temperature was found for city=<{0}> and the current time";
}
