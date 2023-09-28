package ru.bukhtaev.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.util.NotEnoughSpaceException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.bukhtaev.TestUtils.createTestData;

/**
 * Тестовый класс для репозитория {@link InMemoryRepository}
 */
class InMemoryRepositoryTest {

    /**
     * Регионы для генерации.
     */
    private static final Map<UUID, String> REGIONS = Map.of(
            UUID.randomUUID(), "Region A",
            UUID.randomUUID(), "Region B",
            UUID.randomUUID(), "Region C"
    );

    /**
     * Вместимость репозитория.
     */
    private static final int CAPACITY = 4;

    /**
     * Тестируемый CSV репозиторий.
     */
    private Repository<Weather> underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    @BeforeEach
    public void setUp() {
        underTest = new InMemoryRepository(CAPACITY);
        underTest.clear();

        weather1 = createTestData(REGIONS, 28.57);
        weather2 = createTestData(REGIONS, -5.68);
        weather3 = createTestData(REGIONS, 0.83);
    }

    @Test
    void findAll_shouldReturnFoundObjects() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
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
    void saveAll_withEnoughFreeSpace_shouldSaveObjects() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);
        final Weather weather4 = createTestData(REGIONS, -34.7);
        final List<Weather> additionalData = List.of(weather4);

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
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        underTest.saveAll(testData);
        final Weather weather4 = createTestData(REGIONS, -8.54);
        final Weather weather5 = createTestData(REGIONS, 17.13);
        final List<Weather> additionalData = List.of(weather4, weather5);

        // when
        // then
        assertThrows(
                NotEnoughSpaceException.class,
                () -> underTest.saveAll(additionalData),
                "Not enough storage space! Free storage space: " + (CAPACITY - testData.size())
        );
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
        final Weather weather4 = createTestData(REGIONS, 14.9);

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
        final Weather weather4 = createTestData(REGIONS, -6.3);
        final Weather weather5 = createTestData(REGIONS, 36.6);
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3,
                weather4
        );
        underTest.saveAll(testData);

        // when
        // then
        assertThrows(
                NotEnoughSpaceException.class,
                () -> underTest.save(weather5),
                "There is no storage space!"
        );
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
    void constructor_withInvalidCapacity_shouldThrowException() {
        // given
        final int capacity = 0;

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> new InMemoryRepository(capacity),
                "Invalid capacity: " + capacity
        );
    }
}
