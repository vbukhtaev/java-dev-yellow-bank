package ru.bukhtaev.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.service.WeatherService;
import ru.bukhtaev.util.DataNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

/**
 * Тестовый класс для сервиса {@link WeatherServiceImpl}
 */
class WeatherServiceImplTest extends AbstractServiceTest {

    /**
     * Дата и время для измерений.
     */
    private static final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    @Mock
    private Repository<Weather> repository;

    /**
     * Тестируемый сервис обработки данных о погоде.
     */
    private WeatherService underTest;

    @BeforeEach
    void setUp() {
        underTest = new WeatherServiceImpl(repository);
    }

    @Test
    void averageTemperatures_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.averageTemperatures(),
                "There is no weather data!"
        );
    }

    @Test
    void averageTemperatures_withData_shouldReturnAverageTemperatures() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final List<Weather> testData = List.of(
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(-5.37).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(0.08).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(17.9).dateTime(NOW).build(),

                Weather.builder().regionId(regionBId).regionName(regionB).temperature(25.5).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(-5.5).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(1.0).dateTime(NOW).build()
        );
        given(repository.findAll()).willReturn(testData);

        // when
        final var result = underTest.averageTemperatures();

        // then
        assertEquals(2, result.size());
        assertEquals(4.2, result.get(regionA));
        assertEquals(7.0, result.get(regionB));
    }

    @Test
    void averageTemperature_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.averageTemperature(),
                "There is no weather data!"
        );
    }

    @Test
    void averageTemperature_withData_shouldReturnAverageTemperature() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final List<Weather> testData = List.of(
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(-5.37).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(0.08).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(17.9).dateTime(NOW).build()
        );
        given(repository.findAll()).willReturn(testData);

        // when
        var result = underTest.averageTemperature();

        // then
        assertEquals(4.20, result, 0.01);
    }

    @Test
    void getRegionsWarmerThan_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.getRegionsWarmerThan(20.0),
                "There is no weather data!"
        );
    }

    @Test
    void getRegionsWarmerThan_withData_shouldReturnRegionsWarmerThanSpecifiedTemperature() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final String regionC = "Region C";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final UUID regionCId = UUID.randomUUID();
        final List<Weather> testData = List.of(
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(25.37).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(30.08).dateTime(NOW).build(),

                Weather.builder().regionId(regionBId).regionName(regionB).temperature(17.9).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(25.5).dateTime(NOW).build(),

                Weather.builder().regionId(regionCId).regionName(regionC).temperature(19.99).dateTime(NOW).build()
        );
        given(repository.findAll()).willReturn(testData);

        // when
        final var result = underTest.getRegionsWarmerThan(20.0);

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(regionA));
        assertTrue(result.contains(regionB));
        assertFalse(result.contains(regionC));
    }

    @Test
    void getRegionsStrictlyWarmerThan_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.getRegionsStrictlyWarmerThan(20.0),
                "There is no weather data!"
        );
    }

    @Test
    void getRegionsStrictlyWarmerThan_withData_shouldReturnRegionsStrictlyWarmerThanSpecifiedTemperature() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final String regionC = "Region C";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final UUID regionCId = UUID.randomUUID();
        final List<Weather> testData = List.of(
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(25.37).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(30.08).dateTime(NOW).build(),

                Weather.builder().regionId(regionBId).regionName(regionB).temperature(17.9).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(25.5).dateTime(NOW).build(),

                Weather.builder().regionId(regionCId).regionName(regionC).temperature(19.99).dateTime(NOW).build()
        );
        given(repository.findAll()).willReturn(testData);

        // when
        var result = underTest.getRegionsStrictlyWarmerThan(20.0);

        // then
        assertEquals(1, result.size());
        assertTrue(result.contains(regionA));
    }

    @Test
    void groupTemperaturesById_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.groupTemperaturesById(),
                "There is no weather data!"
        );
    }

    @Test
    void groupTemperaturesById_withData_shouldReturnTemperaturesGroupedById() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final String regionC = "Region C";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final UUID regionCId = UUID.randomUUID();
        final double tempA1 = 25.37;
        final double tempA2 = 30.08;
        final double tempB1 = 17.9;
        final double tempB2 = 25.5;
        final double tempC1 = 19.99;
        final List<Weather> testData = List.of(
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(tempA1).dateTime(NOW).build(),
                Weather.builder().regionId(regionAId).regionName(regionA).temperature(tempA2).dateTime(NOW).build(),

                Weather.builder().regionId(regionBId).regionName(regionB).temperature(tempB1).dateTime(NOW).build(),
                Weather.builder().regionId(regionBId).regionName(regionB).temperature(tempB2).dateTime(NOW).build(),

                Weather.builder().regionId(regionCId).regionName(regionC).temperature(tempC1).dateTime(NOW).build()
        );
        given(repository.findAll()).willReturn(testData);

        // when
        var result = underTest.groupTemperaturesById();

        // then
        assertEquals(3, result.size());
        assertTrue(result.containsKey(regionAId));
        assertTrue(result.containsKey(regionBId));
        assertTrue(result.containsKey(regionCId));
        assertEquals(2, result.get(regionAId).size());
        assertEquals(2, result.get(regionBId).size());
        assertEquals(1, result.get(regionCId).size());
        assertTrue(result.get(regionAId).containsAll(List.of(tempA1, tempA2)));
        assertTrue(result.get(regionBId).containsAll(List.of(tempB1, tempB2)));
        assertTrue(result.get(regionCId).contains(tempC1));
    }

    @Test
    void groupByTemperature_withNoData_shouldThrowException() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        // then
        assertThrows(
                DataNotFoundException.class,
                () -> underTest.groupByTemperature(),
                "There is no weather data!"
        );
    }

    @Test
    void groupByTemperature_withData_shouldReturnObjectsGroupedByTemperature() {
        // given
        final String regionA = "Region A";
        final String regionB = "Region B";
        final String regionC = "Region C";
        final UUID regionAId = UUID.randomUUID();
        final UUID regionBId = UUID.randomUUID();
        final UUID regionCId = UUID.randomUUID();
        final Weather weather1 = Weather.builder().regionId(regionAId).regionName(regionA).temperature(-24.6).dateTime(NOW).build();
        final Weather weather2 = Weather.builder().regionId(regionAId).regionName(regionA).temperature(30.08).dateTime(NOW).build();
        final Weather weather3 = Weather.builder().regionId(regionBId).regionName(regionB).temperature(17.9).dateTime(NOW).build();
        final Weather weather4 = Weather.builder().regionId(regionBId).regionName(regionB).temperature(-25.4).dateTime(NOW).build();
        final Weather weather5 = Weather.builder().regionId(regionCId).regionName(regionC).temperature(29.5).dateTime(NOW).build();
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4,
                weather5
        );
        given(repository.findAll()).willReturn(testData);

        // when
        var result = underTest.groupByTemperature();

        // then
        assertEquals(3, result.size());
        assertTrue(result.containsKey(30));
        assertTrue(result.containsKey(-25));
        assertTrue(result.containsKey(18));
        assertEquals(2, result.get(30).size());
        assertEquals(2, result.get(-25).size());
        assertEquals(1, result.get(18).size());
        assertTrue(result.get(30).containsAll(List.of(weather2, weather5)));
        assertTrue(result.get(-25).containsAll(List.of(weather1, weather4)));
        assertTrue(result.get(18).contains(weather3));
    }
}
