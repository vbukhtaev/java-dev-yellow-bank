package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static ru.bukhtaev.TestUtils.*;
import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * Модульные тесты для сервиса генерации данных о погоде {@link GenerationServiceImpl}
 */
class GenerationServiceImplTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый сервис генерации данных о погоде.
     */
    @InjectMocks
    private GenerationServiceImpl underTest;

    private List<City> cities;
    private List<WeatherType> types;

    @BeforeEach
    void setUp() {
        cities = List.of(
                City.builder().id(UUID.randomUUID()).name("Казань").build(),
                City.builder().id(UUID.randomUUID()).name("Екатеринбург").build(),
                City.builder().id(UUID.randomUUID()).name("Новосибирск").build()
        );

        types = List.of(
                WeatherType.builder().id(UUID.randomUUID()).name("Ясно").build(),
                WeatherType.builder().id(UUID.randomUUID()).name("Пасмурно").build(),
                WeatherType.builder().id(UUID.randomUUID()).name("Метель").build()
        );
    }

    @Test
    void generate_withValidParams_shouldGenerateData() {
        // given
        final int count = 4;

        // when
        final List<Weather> result = underTest.generate(
                cities,
                types,
                count
        );

        // then
        assertEquals(count, result.size());
        for (final Weather weather : result) {
            assertNotNull(weather);
            assertNotNull(weather.getCity().getId());
            assertNotNull(weather.getCity().getName());
            assertTrue(cities.contains(weather.getCity()));
            assertNotNull(weather.getType().getId());
            assertNotNull(weather.getType().getName());
            assertTrue(types.contains(weather.getType()));
            assertNotNull(weather.getTemperature());
            assertNotNull(weather.getDateTime());
        }
    }

    @Test
    void generate_withEmptyCitiesList_shouldThrowException() {
        // given
        final int count = 4;
        final List<City> emptyCities = Collections.emptyList();
        given(messageProvider.getMessage(MESSAGE_CODE_EMPTY_CITIES_LIST))
                .willReturn(MESSAGE_EMPTY_CITIES_LIST);

        // when
        // then
        assertThatThrownBy(() -> underTest.generate(emptyCities, types, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MESSAGE_EMPTY_CITIES_LIST);
    }

    @Test
    void generate_withEmptyTypesList_shouldThrowException() {
        // given
        final int count = 4;
        final List<WeatherType> emptyTypes = Collections.emptyList();
        given(messageProvider.getMessage(MESSAGE_CODE_EMPTY_TYPES_LIST))
                .willReturn(MESSAGE_EMPTY_TYPES_LIST);

        // when
        // then
        assertThatThrownBy(() -> underTest.generate(cities, emptyTypes, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MESSAGE_EMPTY_TYPES_LIST);
    }

    @Test
    void generate_withInvalidCount_shouldThrowException() {
        // given
        final int count = 0;
        final String exceptionMessage = MessageFormat.format(MESSAGE_INCORRECT_COUNT, count);
        given(messageProvider.getMessage(MESSAGE_CODE_INCORRECT_COUNT, count))
                .willReturn(exceptionMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.generate(cities, types, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }
}
