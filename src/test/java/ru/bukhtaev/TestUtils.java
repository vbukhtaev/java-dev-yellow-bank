package ru.bukhtaev;

import ru.bukhtaev.service.GenerationServiceImpl;
import ru.bukhtaev.service.crud.IWeatherCrudService;

import java.time.temporal.ChronoUnit;
import java.util.List;

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
     * Сообщение о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передан пустой список городов.
     */
    public static final String MESSAGE_EMPTY_CITIES_LIST = "Empty cities list!";

    /**
     * Сообщение о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передан пустой список типов погоды.
     */
    public static final String MESSAGE_EMPTY_TYPES_LIST = "Empty types list!";

    /**
     * Сообщение о том, что в качестве аргумента в метод {@link GenerationServiceImpl#generate(List, List, int)}
     * передано некорректное количество записей.
     */
    public static final String MESSAGE_INCORRECT_COUNT = "Invalid count: {0}";

    /**
     * Сообщение о том, что методу {@link IWeatherCrudService#getTemperature(String, ChronoUnit)}
     * не удалось найти запрашиваемые данные.
     */
    public static final String MESSAGE_TEMPERATURE_NOT_FOUND
            = "No temperature was found for city=<{0}> and the current time";
}
