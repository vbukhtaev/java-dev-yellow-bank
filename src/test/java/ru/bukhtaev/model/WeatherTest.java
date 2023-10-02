package ru.bukhtaev.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .cityId(UUID.randomUUID())
                .cityName("City1")
                .temperature(20.5)
                .dateTime(LocalDateTime.now())
                .build();

        weather2 = Weather.builder()
                .cityId(UUID.randomUUID())
                .cityName("City1")
                .temperature(20.5)
                .dateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testToString() {
        // given
        final String expectedString = String.format(
                "Weather(%s, '%s', %s°C, %s)",
                weather1.getCityId(),
                weather1.getCityName(),
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
                .cityId(weather1.getCityId())
                .cityName(weather1.getCityName())
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
                .cityId(weather1.getCityId())
                .cityName(weather1.getCityName())
                .temperature(weather1.getTemperature())
                .dateTime(weather1.getDateTime())
                .build();

        // when
        // then
        assertEquals(weather1.hashCode(), weather3.hashCode());
    }
}
