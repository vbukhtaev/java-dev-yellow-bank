package ru.bukhtaev.service.crud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.InvalidPropertyException;
import ru.bukhtaev.exception.UniqueWeatherException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.service.AbstractServiceTest;
import ru.bukhtaev.service.WeatherCache;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.bukhtaev.model.Weather.FIELD_CITY;
import static ru.bukhtaev.model.Weather.FIELD_TYPE;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;
import static ru.bukhtaev.validation.MessageUtils.*;

/**
 * Модульные тесты для JPA-реализации сервиса CRUD операций
 * над данными о погоде {@link WeatherCrudServiceJpaImpl}.
 */
class WeatherCrudServiceJpaImplTest extends AbstractServiceTest {

    /**
     * Имитация сервиса предоставления сообщений.
     */
    @Mock
    private MessageProvider messageProvider;

    /**
     * Имитация сервиса, предоставляющего LRU-кэш для данных о погоде.
     */
    @Mock
    private WeatherCache cache;

    /**
     * Имитация JPA-репозитория данных о погоде.
     */
    @Mock
    private IWeatherJpaRepository weatherRepository;

    /**
     * Имитация JPA-репозитория городов.
     */
    @Mock
    private ICityJpaRepository cityRepository;

    /**
     * Имитация JPA-репозитория типов погоды.
     */
    @Mock
    private IWeatherTypeJpaRepository typeRepository;

    /**
     * Тестируемая JPA-реализация сервиса CRUD операций над данными о погоде.
     */
    @InjectMocks
    private WeatherCrudServiceJpaImpl underTest;

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
    void getById_withExistentId_shouldReturnEntityAndPutToCache() {
        // given
        final UUID weather1Id = weather1.getId();
        given(cache.get(weather1Id))
                .willReturn(Optional.empty());
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));

        // when
        underTest.getById(weather1Id);

        // then
        verify(weatherRepository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(idCaptor.getValue())
                .isEqualTo(weather1Id);
        verify(cache, times(1))
                .get(idCaptor.capture());
        assertThat(idCaptor.getValue())
                .isEqualTo(weather1Id);
        verify(cache, times(1))
                .put(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather1);
        verifyNoMoreInteractions(cache);
    }

    @Test
    void getById_withExistentIdFromCache_shouldReturnEntityFromCache() {
        // given
        final UUID weather1Id = weather1.getId();
        given(cache.get(weather1Id))
                .willReturn(Optional.of(weather1));

        // when
        underTest.getById(weather1Id);

        // then
        verifyNoInteractions(weatherRepository);
        verify(cache, times(1))
                .get(idCaptor.capture());
        verifyNoMoreInteractions(cache);
        assertThat(idCaptor.getValue())
                .isEqualTo(weather1Id);
        verifyNoMoreInteractions(cache);
    }

    @Test
    void getById_withNonExistentId_shouldThrowException() {
        // given
        final UUID weather2Id = weather2.getId();
        final String errorMessage = MessageFormat.format(
                "Weather with ID = <{0}> not found!",
                weather2Id
        );
        given(cache.get(weather2Id))
                .willReturn(Optional.empty());
        given(weatherRepository.findById(weather2Id))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_NOT_FOUND,
                weather2Id
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.getById(weather2Id))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);

        verify(cache, times(1))
                .get(idCaptor.capture());
        assertThat(idCaptor.getValue())
                .isEqualTo(weather2Id);
        verify(weatherRepository, times(1))
                .findById(idCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(idCaptor.getValue())
                .isEqualTo(weather2Id);
    }

    @Test
    void getAll_shouldReturnAllEntities() {
        // given
        given(weatherRepository.findAll())
                .willReturn(List.of(
                        weather1,
                        weather2,
                        weather3
                ));

        // when
        underTest.getAll();

        // then
        verify(weatherRepository, times(1)).findAll();
        verifyNoMoreInteractions(weatherRepository);
    }

    @Test
    void create_withNonExistentCityAndDateTimeCombination_shouldCreateEntityAndPutToCache() {
        // given
        given(weatherRepository.findFirstByCityIdAndDateTime(
                weather1.getCity().getId(),
                weather1.getDateTime()
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityKazan.getId()))
                .willReturn(Optional.of(cityKazan));
        given(typeRepository.findById(typeClear.getId()))
                .willReturn(Optional.of(typeClear));
        given(weatherRepository.save(any()))
                .willReturn(weather1);

        // when
        underTest.create(weather1);

        // then
        verify(weatherRepository, times(1))
                .save(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather1);
        verify(cache, times(1))
                .put(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather1);
        verifyNoMoreInteractions(cache);
    }

    @Test
    void create_withExistentCityAndDateTimeCombination_shouldThrowException() {
        // given
        final UUID cityId = weather1.getCity().getId();
        final var dateTime = weather1.getDateTime();
        final String errorMessage = MessageFormat.format(
                "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                cityId,
                dateTime
        );
        given(weatherRepository.findFirstByCityIdAndDateTime(
                cityId,
                dateTime
        )).willReturn(Optional.of(weather1));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                cityId,
                dateTime.format(DATE_TIME_FORMATTER)
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(weather1))
                .isInstanceOf(UniqueWeatherException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void create_withIncorrectCityId_shouldThrowException() {
        // given
        weather1.getCity().setId(null);
        final String errorMessage = "Invalid field value!";
        given(messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD))
                .willReturn(errorMessage);

        // when
        // then
        final var exception = catchThrowableOfType(
                () -> underTest.create(weather1),
                InvalidPropertyException.class
        );
        assertThat(exception.getErrorMessage())
                .isEqualTo(errorMessage);
        assertThat(exception.getParamNames())
                .containsExactlyInAnyOrder(FIELD_CITY);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void create_withIncorrectTypeId_shouldThrowException() {
        // given
        weather1.getType().setId(null);
        final String errorMessage = "Invalid field value!";
        given(messageProvider.getMessage(MESSAGE_CODE_INVALID_FIELD))
                .willReturn(errorMessage);

        // when
        // then
        final var exception = catchThrowableOfType(
                () -> underTest.create(weather1),
                InvalidPropertyException.class
        );
        assertThat(exception.getErrorMessage())
                .isEqualTo(errorMessage);
        assertThat(exception.getParamNames())
                .containsExactlyInAnyOrder(FIELD_TYPE);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void create_withNonExistentCityId_shouldThrowException() {
        // given
        final UUID cityId = weather1.getCity().getId();
        final var dateTime = weather1.getDateTime();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityId
        );
        given(weatherRepository.findFirstByCityIdAndDateTime(
                cityId,
                dateTime
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityKazan.getId()))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityKazan.getId()
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(weather1))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void create_withNonExistentTypeId_shouldThrowException() {
        // given
        final UUID typeId = typeClear.getId();
        final UUID cityId = cityKazan.getId();
        final var dateTime = weather1.getDateTime();
        final String errorMessage = MessageFormat.format(
                "Weather type with ID = <{0}> not found!",
                typeId
        );
        given(weatherRepository.findFirstByCityIdAndDateTime(
                cityId,
                dateTime
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.of(cityKazan));
        given(typeRepository.findById(typeClear.getId()))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeClear.getId()
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(weather1))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void delete_withIdArgument_shouldDeleteMatchingEntityAndDeleteFromCache() {
        // given
        final UUID weather1Id = weather1.getId();
        given(weatherRepository.deleteAllById(weather1Id))
                .willReturn(List.of(weather1));

        // when
        underTest.delete(weather1Id);

        // then
        verify(weatherRepository, times(1))
                .deleteAllById(idCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(idCaptor.getValue())
                .isEqualTo(weather1Id);
        verify(cache, times(1))
                .delete(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather1);
        verifyNoMoreInteractions(cache);
    }

    @Test
    void update_withExistentCityAndDateTimeCombination_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                cityId,
                dateTime
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.of(weather1));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                cityId,
                dateTime.format(DATE_TIME_FORMATTER)
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(weather1Id, weather2))
                .isInstanceOf(UniqueWeatherException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void update_withNonExistentCityAndDateTimeCombinationAndNonExistentId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final String errorMessage = MessageFormat.format(
                "Weather with ID = <{0}> not found!",
                weather1Id
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_NOT_FOUND,
                weather1Id
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void update_withNonExistentCityId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityId
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void update_withNonExistentTypeId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final UUID typeId = weather2.getType().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "WeatherType with ID = <{0}> not found!",
                cityId
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.of(cityYekaterinburg));
        given(typeRepository.findById(typeId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.update(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void update_withValidData_shouldUpdateEntityAndPutToCache() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final UUID typeId = weather2.getType().getId();
        final var dateTime = weather2.getDateTime();
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.of(cityYekaterinburg));
        given(typeRepository.findById(typeId))
                .willReturn(Optional.of(typeBlizzard));
        given(weatherRepository.save(weather1))
                .willReturn(weather1);

        // when
        underTest.update(weather1Id, weather2);

        // then
        verify(weatherRepository, times(1))
                .save(weatherCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather2);
        verify(cache, times(1))
                .put(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather2);
        verifyNoMoreInteractions(cache);
    }

    @Test
    void replace_withExistentCityAndDateTimeCombination_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                cityId,
                dateTime
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.of(weather1));
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_UNIQUE_CITY_AND_TIME,
                cityId,
                dateTime.format(DATE_TIME_FORMATTER)
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.replace(weather1Id, weather2))
                .isInstanceOf(UniqueWeatherException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void replace_withNonExistentCityAndDateTimeCombinationAndNonExistentId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final String errorMessage = MessageFormat.format(
                "Weather with ID = <{0}> not found!",
                weather1Id
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_NOT_FOUND,
                weather1Id
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.replace(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void replace_withNonExistentCityId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "City with ID = <{0}> not found!",
                cityId
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_CITY_NOT_FOUND,
                cityId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.replace(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void replace_withNonExistentTypeId_shouldThrowException() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final UUID typeId = weather2.getType().getId();
        final var dateTime = weather2.getDateTime();
        final String errorMessage = MessageFormat.format(
                "WeatherType with ID = <{0}> not found!",
                cityId
        );
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.of(cityYekaterinburg));
        given(typeRepository.findById(typeId))
                .willReturn(Optional.empty());
        given(messageProvider.getMessage(
                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                typeId
        )).willReturn(errorMessage);

        // when
        // then
        assertThatThrownBy(() -> underTest.replace(weather1Id, weather2))
                .isInstanceOf(DataNotFoundException.class)
                .extracting(ERROR_MESSAGE_PROPERTY_NAME)
                .isEqualTo(errorMessage);
        verify(weatherRepository, never()).save(any());
        verifyNoInteractions(cache);
    }

    @Test
    void replace_withValidDate_shouldReplaceEntityAndPutToCache() {
        // given
        final UUID weather1Id = weather1.getId();
        final UUID cityId = weather2.getCity().getId();
        final UUID typeId = weather2.getType().getId();
        final var dateTime = weather2.getDateTime();
        given(weatherRepository.findById(weather1Id))
                .willReturn(Optional.of(weather1));
        given(weatherRepository.findFirstByCityIdAndDateTimeAndIdNot(
                cityId,
                dateTime,
                weather1Id
        )).willReturn(Optional.empty());
        given(cityRepository.findById(cityId))
                .willReturn(Optional.of(cityYekaterinburg));
        given(typeRepository.findById(typeId))
                .willReturn(Optional.of(typeBlizzard));
        given(weatherRepository.save(weather1))
                .willReturn(weather1);

        // when
        underTest.replace(weather1Id, weather2);

        // then
        verify(weatherRepository, times(1))
                .save(weatherCaptor.capture());
        verifyNoMoreInteractions(weatherRepository);
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather2);
        verify(cache, times(1))
                .put(weatherCaptor.capture());
        assertThat(weatherCaptor.getValue())
                .isEqualTo(weather2);
        verifyNoMoreInteractions(cache);
    }
}