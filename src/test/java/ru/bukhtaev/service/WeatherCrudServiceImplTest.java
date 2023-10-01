package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;
import ru.bukhtaev.util.DataNotFoundException;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.bukhtaev.TestUtils.MESSAGE_TEMPERATURE_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_TEMPERATURE_NOT_FOUND;

/**
 * Модульные тесты для сервиса {@link WeatherCrudServiceImpl}
 */
class WeatherCrudServiceImplTest extends AbstractServiceTest {

    /**
     * Имитируемый репозиторий.
     */
    @Mock
    private IRepository<Weather> repository;

    /**
     * Имитируемый сервис сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый сервис CRUD операций с данными о погоде.
     */
    @InjectMocks
    private WeatherCrudServiceImpl underTest;

    @Captor
    private ArgumentCaptor<Weather> weatherCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    @BeforeEach
    void setUp() {
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        weather1 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        weather2 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build();
        weather3 = Weather.builder().cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build();
    }

    @Test
    void getTemperatures_withExistentWeatherData_shouldReturnExistentData() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        given(repository.findAll()).willReturn(testData);

        // when
        final List<Weather> result = underTest.getTemperatures(weather1.getCityName());

        // then
        assertEquals(1, result.size());
        assertTrue(result.contains(weather1));
    }

    @Test
    void getTemperatures_withNonExistentWeatherData_shouldNotReturnAnyData() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        given(repository.findAll()).willReturn(testData);

        // when
        final List<Weather> result = underTest.getTemperatures("Another city");

        // then
        assertEquals(0, result.size());
    }

    @Test
    void getTemperature_withExistentWeatherData_shouldReturnTemperatureOfExistentWeatherData() {
        // given
        final String cityName = weather1.getCityName();
        final Double temperature = weather1.getTemperature();
        final ChronoUnit timeUnit = ChronoUnit.MINUTES;
        given(repository.findFirst(any()))
                .willReturn(Optional.of(weather1));

        // when
        final Double result = underTest.getTemperature(cityName, timeUnit);

        // then
        verify(repository, times(1)).findFirst(any());
        verifyNoMoreInteractions(repository);
        assertEquals(temperature, result);
    }

    @Test
    void getTemperature_withNonExistentWeatherData_shouldThrowException() {
        // given
        final String cityName = weather1.getCityName();
        final ChronoUnit timeUnit = ChronoUnit.MINUTES;
        given(repository.findFirst(any()))
                .willReturn(Optional.empty());
        final String exceptionMessage = MessageFormat.format(MESSAGE_TEMPERATURE_NOT_FOUND, cityName);
        given(messageProvider.getMessage(MESSAGE_CODE_TEMPERATURE_NOT_FOUND, cityName))
                .willReturn(exceptionMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.getTemperature(cityName, timeUnit))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(exceptionMessage);
    }

    @Test
    void remove_shouldRemoveAllWeatherDataWithSpecifiedCityName() {
        // given
        final String cityName = weather1.getCityName();

        // when
        underTest.remove(cityName);

        // then
        verify(repository, times(1)).remove(stringCaptor.capture());
        verifyNoMoreInteractions(repository);
        assertEquals(cityName, stringCaptor.getValue());
    }

    @Test
    void create_withNonExistentCityName_shouldCreateNewWeatherData() {
        // given
        given(repository.findFirst(any()))
                .willReturn(Optional.empty());

        // when
        underTest.create(weather1);

        // then
        verify(repository, times(1)).save(weatherCaptor.capture());
        assertEquals(weather1, weatherCaptor.getValue());
    }

    @Test
    void create_withExistentCityName_shouldCreateNewWeatherData() {
        // given
        given(repository.findFirst(any()))
                .willReturn(Optional.of(weather1));

        // when
        underTest.create(weather1);

        // then
        verify(repository, times(1)).save(weatherCaptor.capture());
        assertEquals(weather1, weatherCaptor.getValue());
    }

    @Test
    void update_withExistentWeatherData_shouldUpdateExistentWeatherData() {
        // given
        given(repository.findFirst(any()))
                .willReturn(Optional.of(weather1));
        weather2.setCityId(weather1.getCityId());
        weather2.setCityName(weather1.getCityName());
        weather2.setDateTime(weather1.getDateTime());

        // when
        final Weather updated = underTest.update(weather2);

        // then
        verify(repository, never()).save(any());
        assertEquals(weather2, updated);
    }

    @Test
    void update_withNonExistentWeatherData_shouldCreateNewWeatherData() {
        // given
        given(repository.findFirst(any()))
                .willReturn(Optional.empty());

        // when
        underTest.update(weather2);

        // then
        verify(repository, times(1)).save(weatherCaptor.capture());
        assertEquals(weather2, weatherCaptor.getValue());
    }
}