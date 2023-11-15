package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_TEMPERATURE_NOT_FOUND;

/**
 * Модульные тесты для реализации сервиса
 * обработки данных о погоде {@link WeatherProcessingServiceImpl}
 */
class WeatherProcessingServiceImplTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Имитация JPA-репозитория данных о погоде.
     */
    @Mock
    private IWeatherJpaRepository weatherRepository;

    /**
     * Тестируемая JPA-реализация сервиса CRUD операций над данными о погоде.
     */
    @InjectMocks
    private WeatherProcessingServiceImpl underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    private City cityKazan;
    private City cityYekaterinburg;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        cityKazan = City.builder()
                .id(UUID.randomUUID())
                .name("Казань")
                .build();
        cityYekaterinburg = City.builder()
                .id(UUID.randomUUID())
                .name("Екатеринбург")
                .build();
        typeClear = WeatherType.builder()
                .id(UUID.randomUUID())
                .name("Ясно")
                .build();
        typeBlizzard = WeatherType.builder()
                .id(UUID.randomUUID())
                .name("Метель")
                .build();

        weather1 = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(24.57)
                .dateTime(NOW)
                .build();
        weather2 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeBlizzard)
                .temperature(-28.72)
                .dateTime(YESTERDAY)
                .build();
        weather3 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeClear)
                .temperature(0.46)
                .dateTime(NOW)
                .build();
    }

    @Test
    void delete_withCityNameArgument_shouldDeleteMatchingEntity() {
        // given
        final String cityName = cityYekaterinburg.getName();

        // when
        underTest.delete(cityName);

        // then
        verify(weatherRepository, times(1))
                .deleteAllByCityName(stringCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(stringCaptor.getValue()).isEqualTo(cityName);
    }

    @Test
    void getTemperatures() {
        // given
        final String cityName = cityYekaterinburg.getName();
        given(weatherRepository.findAll())
                .willReturn(List.of(
                        weather1,
                        weather2,
                        weather3
                ));

        // when
        final var foundData = underTest.getTemperatures(cityName);

        // then
        assertThat(foundData).hasSize(1);
        final Weather weather = foundData.get(0);
        assertThat(weather.getId())
                .isEqualTo(weather3.getId());
        assertThat(weather.getCity().getId())
                .isEqualTo(weather3.getCity().getId());
        assertThat(weather.getCity().getName())
                .isEqualTo(weather3.getCity().getName());
        assertThat(weather.getType().getId())
                .isEqualTo(weather3.getType().getId());
        assertThat(weather.getType().getName())
                .isEqualTo(weather3.getType().getName());
        assertThat(weather.getTemperature())
                .isEqualTo(weather3.getTemperature());
        assertThat(weather.getDateTime())
                .isEqualTo(weather3.getDateTime());
    }

    @Test
    void getTemperature_withExistentData_shouldReturnTemperature() {
        // given
        final String cityName = cityYekaterinburg.getName();
        given(weatherRepository.findAllByCityName(cityName))
                .willReturn(List.of(
                        weather2,
                        weather3
                ));

        // when
        final Double temperature = underTest.getTemperature(
                cityName,
                ChronoUnit.MINUTES
        );

        // then
        assertThat(temperature)
                .isEqualTo(weather3.getTemperature());
    }

    @Test
    void getTemperature_withNonExistentData_shouldThrowException() {
        // given
        final String cityName = "Новосибирск";
        final String errorMessage = MessageFormat.format(
                "No temperature was found for city=<{0}> and the current time",
                cityName
        );
        given(weatherRepository.findAllByCityName(cityName))
                .willReturn(Collections.emptyList());
        given(messageProvider.getMessage(
                MESSAGE_CODE_TEMPERATURE_NOT_FOUND,
                cityName
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(
                () -> underTest.getTemperature(
                        cityName,
                        ChronoUnit.MINUTES
                ))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
    }
}