package ru.bukhtaev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.validation.MessageProvider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.mockito.BDDMockito.given;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_INVALID_DATA_TO_BE_CACHED;

/**
 * Модульные тесты для сервиса, предоставляющего потокобезопасный
 * LRU-кэш для данных о погоде {@link WeatherCache}.
 */
class WeatherCacheTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый сервис, предоставляющий
     * потокобезопасный LRU-кэш для данных о погоде.
     */
    private WeatherCache underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    private City cityKazan;
    private City cityYekaterinburg;
    private City cityNovosibirsk;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        underTest = new WeatherCache(2, messageProvider);

        cityKazan = City.builder()
                .id(UUID.randomUUID())
                .name("Казань")
                .build();
        cityYekaterinburg = City.builder()
                .id(UUID.randomUUID())
                .name("Екатеринбург")
                .build();
        cityNovosibirsk = City.builder()
                .id(UUID.randomUUID())
                .name("Новосибирск")
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
                .id(UUID.randomUUID())
                .city(cityKazan)
                .type(typeClear)
                .temperature(24.57)
                .dateTime(NOW)
                .build();
        weather2 = Weather.builder()
                .id(UUID.randomUUID())
                .city(cityYekaterinburg)
                .type(typeBlizzard)
                .temperature(-28.72)
                .dateTime(YESTERDAY)
                .build();
        weather3 = Weather.builder()
                .id(UUID.randomUUID())
                .city(cityNovosibirsk)
                .type(typeClear)
                .temperature(0.46)
                .dateTime(NOW)
                .build();
    }

    @Test
    void get_withExistentIdArgument_shouldReturnEntityFromCache() {
        // given
        underTest.put(weather1);

        // when
        final Optional<Weather> retrieved = underTest.get(weather1.getId());

        // then
        assertThat(retrieved).contains(weather1);
    }

    @Test
    void get_withNonExistentIdArgument_shouldReturnEmptyOptional() {
        // given
        underTest.put(weather1);

        // when
        final Optional<Weather> retrieved = underTest.get(weather2.getId());

        // then
        assertThat(retrieved).isNotPresent();
    }

    @Test
    void get_withExistentIdArgumentAndCacheOverflow_shouldReturnEmptyOptional() {
        // given
        underTest.put(weather1);
        underTest.put(weather2);
        underTest.put(weather3);

        // when
        final Optional<Weather> retrieved = underTest.get(weather1.getId());

        // then
        assertThat(retrieved).isNotPresent();
    }

    @Test
    void get_withCityNameArgument_shouldReturnEntityFromCache() {
        // given
        underTest.put(weather1);

        // when
        final Optional<Weather> retrieved = underTest.get(cityKazan.getName());

        // then
        assertThat(retrieved).contains(weather1);
    }

    @Test
    void get_withNonCityNameArgument_shouldReturnEmptyOptional() {
        // given
        underTest.put(weather1);

        // when
        final Optional<Weather> retrieved = underTest.get(cityYekaterinburg.getName());

        // then
        assertThat(retrieved).isNotPresent();
    }

    @Test
    void get_withCityNameArgumentAndCacheOverflow_shouldReturnEmptyOptional() {
        // given
        underTest.put(weather1);
        underTest.put(weather2);
        underTest.put(weather3);

        // when
        final Optional<Weather> retrieved = underTest.get(cityKazan.getName());

        // then
        assertThat(retrieved).isNotPresent();
    }

    @Test
    void put_withValidData_shouldPutEntityToBothCaches() {
        // given
        assertThat(underTest.get(weather1.getId())).isNotPresent();
        assertThat(underTest.get(cityKazan.getName())).isNotPresent();

        // when
        final Weather cached = underTest.put(weather1);

        // then
        assertThat(cached).isEqualTo(weather1);
        assertThat(underTest.get(weather1.getId())).contains(weather1);
        assertThat(underTest.get(cityKazan.getName())).contains(weather1);
    }

    @Test
    void put_withInvalidData_shouldThrowException() {
        // given
        weather1.setId(null);
        final String errorMessage = "Invalid data provided for caching!";
        given(messageProvider.getMessage(MESSAGE_CODE_INVALID_DATA_TO_BE_CACHED))
                .willReturn(errorMessage);

        // when
        // then
        final var exception = catchThrowableOfType(
                () -> underTest.put(weather1),
                IllegalArgumentException.class
        );
        assertThat(exception.getMessage())
                .isEqualTo(errorMessage);
    }

    @Test
    void delete_withValidData_shouldDeleteEntityFromBothCaches() {
        // given
        underTest.put(weather1);
        assertThat(underTest.get(weather1.getId())).contains(weather1);
        assertThat(underTest.get(cityKazan.getName())).contains(weather1);

        // when
        underTest.delete(weather1);

        // then
        assertThat(underTest.get(weather1.getId())).isNotPresent();
        assertThat(underTest.get(cityKazan.getName())).isNotPresent();
    }

    @Test
    void delete_withInvalidData_shouldThrowException() {
        // given
        weather1.setId(null);
        final String errorMessage = "Invalid data provided for caching!";
        given(messageProvider.getMessage(MESSAGE_CODE_INVALID_DATA_TO_BE_CACHED))
                .willReturn(errorMessage);

        // when
        // then
        final var exception = catchThrowableOfType(
                () -> underTest.delete(weather1),
                IllegalArgumentException.class
        );
        assertThat(exception.getMessage())
                .isEqualTo(errorMessage);
    }
}
