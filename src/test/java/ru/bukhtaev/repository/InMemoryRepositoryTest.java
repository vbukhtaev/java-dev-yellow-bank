package ru.bukhtaev.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.util.NotEnoughSpaceException;
import ru.bukhtaev.validation.MessageProvider;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_INVALID_CAPACITY;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_NOT_ENOUGH_STORAGE_SPACE;

/**
 * Модульные тесты для репозитория {@link InMemoryRepository}
 */
class InMemoryRepositoryTest {

    /**
     * Вместимость репозитория.
     */
    private static final int CAPACITY = 4;

    @Mock
    private MessageProvider messageProvider;

    /**
     * Тестируемый репозиторий.
     */
    @InjectMocks
    private IRepository<Weather> underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;
    private Weather weather4;
    private Weather weather5;

    @BeforeEach
    public void setUp() {
        underTest = new InMemoryRepository(CAPACITY);

        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final String cityD = "City D";
        final String cityE = "City E";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final UUID cityIdD = UUID.randomUUID();
        final UUID cityIdE = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        weather1 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        weather2 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build();
        weather3 = Weather.builder().cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build();
        weather4 = Weather.builder().cityId(cityIdD).cityName(cityD).temperature(-0.84).dateTime(now).build();
        weather5 = Weather.builder().cityId(cityIdE).cityName(cityE).temperature(28.7).dateTime(now).build();
    }


    @Test
    void findAll_shouldReturnFoundObjects() {
        // given
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final List<Weather> testData = List.of(
                Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build(),
                Weather.builder().cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build(),
                Weather.builder().cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build()
        );
        underTest.saveAll(testData);

        // when
        final List<Weather> retrievedData = underTest.findAll();

        // then
        assertEquals(testData.size(), retrievedData.size());
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i), retrievedData.get(i));
        }
    }

    @Test
    void find_shouldReturnFoundObjects() {
        // given
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();
        final Weather weather1 = Weather.builder()
                .cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        final Weather weather2 = Weather.builder()
                .cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build();
        final Weather weather3 = Weather.builder()
                .cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build();
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        // when
        final List<Weather> retrievedData = underTest.find(
                weather -> weather.getTemperature() > 0
        );

        // then
        assertEquals(2, retrievedData.size());
        assertTrue(retrievedData.containsAll(List.of(weather1, weather3)));
        assertFalse(retrievedData.contains(weather2));
    }

    @Test
    void findFirst_shouldReturnFirstFoundObject() {
        // given
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();
        final Weather weather1 = Weather.builder()
                .cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        final Weather weather2 = Weather.builder()
                .cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build();
        final Weather weather3 = Weather.builder()
                .cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build();
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        // when
        final Optional<Weather> first = underTest.findFirst(
                weather -> weather.getTemperature() > 0
        );

        // then
        assertTrue(first.isPresent());
        assertEquals(first.get(), weather1);
    }

    @Test
    void saveAll_withEnoughFreeSpace_shouldSaveObjects() {
        // given
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final String cityD = "City D";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final UUID cityIdD = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final List<Weather> testData = List.of(
                Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build(),
                Weather.builder().cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build(),
                Weather.builder().cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build()
        );
        underTest.saveAll(testData);

        final List<Weather> additionalData = List.of(
                Weather.builder().cityId(cityIdD).cityName(cityD).temperature(14.9).dateTime(now).build()
        );

        // when
        underTest.saveAll(additionalData);

        // then
        final List<Weather> retrievedData = underTest.findAll();
        final int expectedSize = testData.size() + additionalData.size();
        assertEquals(expectedSize, retrievedData.size());
        assertTrue(retrievedData.containsAll(testData));
        assertTrue(retrievedData.containsAll(additionalData));
    }

    @Test
    void saveAll_withNotEnoughFreeSpace_shouldThrowException() {
        // given
        final String cityA = "City A";
        final String cityB = "City B";
        final String cityC = "City C";
        final String cityD = "City D";
        final String cityE = "City E";
        final UUID cityIdA = UUID.randomUUID();
        final UUID cityIdB = UUID.randomUUID();
        final UUID cityIdC = UUID.randomUUID();
        final UUID cityIdD = UUID.randomUUID();
        final UUID cityIdE = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final List<Weather> testData = List.of(
                Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build(),
                Weather.builder().cityId(cityIdB).cityName(cityB).temperature(-17.9).dateTime(now).build(),
                Weather.builder().cityId(cityIdC).cityName(cityC).temperature(0.16).dateTime(now).build(),
                Weather.builder().cityId(cityIdD).cityName(cityD).temperature(-8.54).dateTime(now).build()
        );
        underTest.saveAll(testData);

        final Weather weather = Weather.builder()
                .cityId(cityIdE).cityName(cityE).temperature(17.13).dateTime(now).build();
        final List<Weather> additionalData = List.of(weather);

        // when
        // then
        final String exceptionMessage = MessageFormat.format(MESSAGE_NOT_ENOUGH_STORAGE_SPACE, 0);
        assertThrows(
                NotEnoughSpaceException.class,
                () -> underTest.saveAll(additionalData),
                exceptionMessage
        );

        final List<Weather> retrievedData = underTest.findAll();
        final int expectedSize = testData.size();
        assertEquals(expectedSize, retrievedData.size());
        assertTrue(retrievedData.containsAll(testData));
        assertFalse(retrievedData.contains(weather));
    }

    @Test
    void save_withEnoughFreeSpace_shouldSaveObjects() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        // when
        underTest.save(weather4);

        // then
        final List<Weather> retrievedData = underTest.findAll();
        final int expectedSize = testData.size() + 1;
        assertEquals(expectedSize, retrievedData.size());
        assertTrue(retrievedData.containsAll(testData));
        assertTrue(retrievedData.contains(weather4));
    }

    @Test
    void save_withNotEnoughFreeSpace_shouldThrowException() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );
        underTest.saveAll(testData);

        // when
        // then
        final String exceptionMessage = MessageFormat.format(MESSAGE_NOT_ENOUGH_STORAGE_SPACE, 0);
        assertThrows(
                NotEnoughSpaceException.class,
                () -> underTest.save(weather5),
                exceptionMessage
        );

        final List<Weather> retrievedData = underTest.findAll();
        final int expectedSize = testData.size();
        assertEquals(expectedSize, retrievedData.size());
        assertTrue(retrievedData.containsAll(testData));
        assertFalse(retrievedData.contains(weather5));
    }

    @Test
    void clear_shouldClearStorage() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        final List<Weather> savedData = underTest.findAll();
        assertEquals(3, savedData.size());

        // when
        underTest.clear();

        // then
        final List<Weather> retrievedData = underTest.findAll();
        assertEquals(0, retrievedData.size());
    }

    @Test
    void remove_shouldRemoveObject() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        // when
        underTest.remove(weather1);

        // then
        final List<Weather> retrievedData = underTest.findAll();
        final int expectedSize = testData.size() - 1;
        assertEquals(expectedSize, retrievedData.size());
        assertTrue(retrievedData.containsAll(
                List.of(weather2, weather3))
        );
        assertFalse(retrievedData.contains(weather1));
    }

    @Test
    void remove_withCityName_shouldRemoveAllObjectsWithSpecifiedCityName() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);

        // when
        underTest.remove(weather1.getCityName());

        // then
        final List<Weather> retrievedData = underTest.findAll();
        assertEquals(2, retrievedData.size());
        assertTrue(retrievedData.containsAll(List.of(weather2, weather3)));
        assertFalse(retrievedData.contains(weather1));
    }

    @Test
    void constructor_withValidCapacity_shouldCreateInstance() {
        // given
        final int capacity = 4;

        // when
        final InMemoryRepository instance = new InMemoryRepository(capacity);

        // then
        assertEquals(capacity, instance.getCapacity());
    }

    @Test
    void constructor_withDefaultCapacity_shouldCreateInstanceWithDefaultCapacity() {
        // given
        final int defaultCapacity = 256;

        // when
        final InMemoryRepository instance = new InMemoryRepository();

        // then
        assertEquals(defaultCapacity, instance.getCapacity());
    }

    @Test
    void constructor_withInvalidCapacity_shouldThrowException() {
        // given
        final int capacity = 0;

        // when
        // then
        final String exceptionMessage = MessageFormat.format(MESSAGE_INVALID_CAPACITY, capacity);
        assertThrows(
                IllegalArgumentException.class,
                () -> new InMemoryRepository(capacity),
                exceptionMessage
        );
    }
}
