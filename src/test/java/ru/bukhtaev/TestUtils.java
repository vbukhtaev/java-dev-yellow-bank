package ru.bukhtaev;

import ru.bukhtaev.model.Weather;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Утилитный класс, содержащий полезные для тестирования методы.
 */
public class TestUtils {

    /**
     * Только для статического использования.
     */
    private TestUtils() {
    }

    /**
     * Создает объект класса {@link Weather} с указанной температурой
     * и случайными значениями остальных параметров.
     *
     * @param temperature температура
     * @return объект класса {@link Weather} с указанной температурой
     * и случайными значениями остальных параметров.
     */
    public static Weather createTestData(final Map<UUID, String> regionsMap, final double temperature) {
        final List<UUID> regionIds = regionsMap.keySet().stream().toList();

        final UUID id = regionIds.get(new Random().nextInt(0, regionIds.size()));

        return Weather.builder()
                .regionId(id)
                .regionName(regionsMap.get(id))
                .temperature(temperature)
                .dateTime(LocalDateTime.now().withNano(0))
                .build();
    }
}
