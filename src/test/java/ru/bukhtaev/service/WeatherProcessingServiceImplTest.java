package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.validation.MessageProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.bukhtaev.TestUtils.MESSAGE_THERE_IS_NO_DATA;

/**
 * Модульные тесты для сервиса {@link WeatherProcessingServiceImpl}
 */
class WeatherProcessingServiceImplTest extends AbstractServiceTest {

    /**
     * Сервис предоставления сообщений/
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый сервис обработки данных о погоде.
     */
    @InjectMocks
    private WeatherProcessingServiceImpl underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;
    private Weather weather4;

    @BeforeEach
    void setUp() {
        final String cityA = "City A";
        final String cityB = "City B";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        weather1 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        weather2 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(-17.9).dateTime(now).build();
        weather3 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(24.7).dateTime(now).build();
        weather4 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(0.84).dateTime(now).build();
    }

    @Test
    void getAverageTemperatures_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.getAverageTemperatures(testData, 2),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void getAverageTemperatures_withData_shouldReturnAverageTemperatures() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.getAverageTemperatures(testData, 2);

        // then
        assertEquals(2, result.size());
        assertEquals(3.74, result.get("City A"));
        assertEquals(12.77, result.get("City B"));
    }

    @Test
    void getAverageTemperature_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.getAverageTemperature(testData, 2),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void getAverageTemperature_withData_shouldReturnAverageTemperature() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.getAverageTemperature(testData, 2);

        // then
        assertEquals(8.25, result);
    }

    @Test
    void getCitiesWarmerThan_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.getCitiesWarmerThan(testData, 20.0),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void getCitiesWarmerThan_withData_shouldReturnCitiesWarmerThanSpecifiedTemperature() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.getCitiesWarmerThan(testData, 15);

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains("City A"));
        assertTrue(result.contains("City B"));
        assertFalse(result.contains("City C"));
    }

    @Test
    void getCitiesStrictlyWarmerThan_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.getCitiesStrictlyWarmerThan(testData, 15),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void getCitiesStrictlyWarmerThan_withData_shouldReturnCitiesStrictlyWarmerThanSpecifiedTemperature() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.getCitiesStrictlyWarmerThan(testData, 0);

        // then
        assertEquals(1, result.size());
        assertTrue(result.contains("City B"));
        assertFalse(result.contains("City A"));
        assertFalse(result.contains("City C"));
    }

    @Test
    void groupTemperaturesById_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.groupTemperaturesById(testData),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void groupTemperaturesById_withData_shouldReturnTemperaturesGroupedById() {
        // given
        final UUID cityIdA = weather1.getCityId();
        final UUID cityIdB = weather3.getCityId();
        final Double tempA1 = weather1.getTemperature();
        final Double tempA2 = weather2.getTemperature();
        final Double tempA3 = weather3.getTemperature();
        final Double tempA4 = weather4.getTemperature();

        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.groupTemperaturesById(testData);

        // then
        assertEquals(2, result.size());
        assertTrue(result.containsKey(cityIdA));
        assertTrue(result.containsKey(cityIdB));
        assertEquals(2, result.get(cityIdA).size());
        assertEquals(2, result.get(cityIdB).size());
        assertTrue(result.get(cityIdA).containsAll(List.of(tempA1, tempA2)));
        assertTrue(result.get(cityIdB).containsAll(List.of(tempA3, tempA4)));
    }

    @Test
    void groupByTemperature_withNoData_shouldThrowException() {
        // given
        final List<Weather> testData = Collections.emptyList();

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.groupByTemperature(testData),
                MESSAGE_THERE_IS_NO_DATA
        );
    }

    @Test
    void groupByTemperature_withData_shouldReturnObjectsGroupedByTemperature() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );

        // when
        final var result = underTest.groupByTemperature(testData);

        // then
        assertEquals(3, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(-18));
        assertTrue(result.containsKey(25));
        assertEquals(1, result.get(1).size());
        assertEquals(1, result.get(-18).size());
        assertEquals(2, result.get(25).size());
        assertTrue(result.get(1).contains(weather4));
        assertTrue(result.get(-18).contains(weather2));
        assertTrue(result.get(25).containsAll(List.of(weather1, weather3)));
    }
}
