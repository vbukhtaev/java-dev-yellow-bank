package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static ru.bukhtaev.TestUtils.MESSAGE_EMPTY_CITIES_SET;
import static ru.bukhtaev.TestUtils.MESSAGE_INCORRECT_COUNT;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_EMPTY_CITIES_SET;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_INCORRECT_COUNT;

/**
 * Тестовый класс для сервиса {@link GenerationServiceImpl}
 */
class GenerationServiceImplTest extends AbstractServiceTest {

    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый сервис генерации данных о погоде.
     */
    @InjectMocks
    private GenerationServiceImpl underTest;

    private String cityA;
    private String cityB;
    private String cityC;

    @BeforeEach
    void setUp() {
        cityA = "City A";
        cityB = "City B";
        cityC = "City C";
    }

    @Test
    void generate_withValidParams_shouldGenerateData() {
        // given
        final Set<String> cities = Set.of(
                cityA,
                cityB,
                cityC
        );
        final int count = 4;

        // when
        final List<Weather> result = underTest.generate(cities, count);

        // then
        assertEquals(count, result.size());
        for (final Weather weather : result) {
            assertNotNull(weather);
            assertNotNull(weather.getCityId());
            assertNotNull(weather.getCityName());
            assertTrue(cities.contains(weather.getCityName()));
            assertNotNull(weather.getTemperature());
            assertNotNull(weather.getDateTime());
        }
    }

    @Test
    void generate_withEmptyCitiesSet_shouldThrowException() {
        // given
        final int count = 4;
        final Set<String> cities = Collections.emptySet();
        given(messageProvider.getMessage(MESSAGE_CODE_EMPTY_CITIES_SET))
                .willReturn(MESSAGE_EMPTY_CITIES_SET);

        // when
        // then
        assertThatThrownBy(() -> underTest.generate(cities, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MESSAGE_EMPTY_CITIES_SET);
    }

    @Test
    void generate_withInvalidCount_shouldThrowException() {
        // given
        final int count = 0;
        final Set<String> cities = Set.of(
                cityA,
                cityB,
                cityC
        );
        final String exceptionMessage = MessageFormat.format(MESSAGE_INCORRECT_COUNT, count);
        given(messageProvider.getMessage(MESSAGE_CODE_INCORRECT_COUNT, count))
                .willReturn(exceptionMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.generate(cities, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }
}
