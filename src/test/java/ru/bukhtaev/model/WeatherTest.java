package ru.bukhtaev.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bukhtaev.repository.impl.InMemoryRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Тестовый класс для модели {@link Weather}
 */
class WeatherTest {

    private Weather weather1;
    private Weather weather2;

    @BeforeEach
    public void setUp() {
        weather1 = Weather.builder()
                .regionId(UUID.randomUUID())
                .regionName("Region1")
                .temperature(20.5)
                .dateTime(LocalDateTime.now())
                .build();

        weather2 = Weather.builder()
                .regionId(UUID.randomUUID())
                .regionName("Region1")
                .temperature(20.5)
                .dateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testToString() {
        // given
        final String expectedString = String.format(
                "Weather(%s, '%s', %s°C, %s)",
                weather1.getRegionId(),
                weather1.getRegionName(),
                weather1.getTemperature(),
                weather1.getDateTime().format(DATE_TIME_FORMATTER)
        );

        // when
        // then
        assertEquals(expectedString, weather1.toString());
    }

    @Test
    void testEquals() {
        // given
        final Weather weather3 = Weather.builder()
                .regionId(weather1.getRegionId())
                .regionName(weather1.getRegionName())
                .temperature(weather1.getTemperature())
                .dateTime(weather1.getDateTime())
                .build();

        // when
        // then
        assertFalse(weather1.equals(weather2));
        assertTrue(weather1.equals(weather3));
    }

    @Test
    void testHashCode() {
        // given
        final Weather weather3 = Weather.builder()
                .regionId(weather1.getRegionId())
                .regionName(weather1.getRegionName())
                .temperature(weather1.getTemperature())
                .dateTime(weather1.getDateTime())
                .build();

        // when
        // then
        assertEquals(weather1.hashCode(), weather3.hashCode());
    }
}
