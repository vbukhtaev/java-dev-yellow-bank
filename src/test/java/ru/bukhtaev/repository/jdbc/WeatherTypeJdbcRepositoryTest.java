package ru.bukhtaev.repository.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.WeatherType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Модульные тесты для JDBC-репозитория типов погоды {@link WeatherTypeJdbcRepository}.
 */
@JdbcTest
class WeatherTypeJdbcRepositoryTest extends AbstractContainerizedTest {

    /**
     * Объект для выполнения SQL-запросов
     * с использованием именованных параметров.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Тестируемый JDBC-репозиторий типов погоды.
     */
    private WeatherTypeJdbcRepository underTest;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        underTest = new WeatherTypeJdbcRepository(jdbcTemplate);

        typeClear = WeatherType.builder()
                .name("Ясно")
                .build();
        typeBlizzard = WeatherType.builder()
                .name("Метель")
                .build();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findById_withExistentId_shouldReturnFoundEntity() {
        // given
        final var saved = underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findById(saved.getId());

        // then
        assertThat(optType).isPresent();
        assertThat(optType.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findById(anotherId);

        // then
        assertThat(optType).isNotPresent();
    }

    @Test
    void findAll_withExistentData_shouldReturnAllEntities() {
        // given
        final var savedBlizzard = underTest.create(typeBlizzard);
        final var savedClear = underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var types = underTest.findAll();

        // then
        assertThat(types).hasSize(2);
        final var blizzard = types.get(0);
        assertThat(blizzard).isNotNull();
        assertThat(blizzard.getId())
                .isEqualTo(savedBlizzard.getId());
        assertThat(blizzard.getName())
                .isEqualTo(typeBlizzard.getName());
        final var clear = types.get(1);
        assertThat(clear).isNotNull();
        assertThat(clear.getId())
                .isEqualTo(savedClear.getId());
        assertThat(clear.getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    void findAll_withNonExistentData_shouldReturnEmptyList() {
        // given
        underTest.deleteAll();
        assertThat(underTest.findAll()).isEmpty();

        // when
        final var types = underTest.findAll();

        // then
        assertThat(types).isEmpty();
    }

    @Test
    void deleteById_withExistentId_shouldDeleteMatchingEntity() {
        // given
        final var saved = underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteById(saved.getId());

        // then
        final var types = underTest.findAll();
        assertThat(types).hasSize(1);
        final var type = types.get(0);
        assertThat(type).isNotNull();
        assertThat(type.getId())
                .isNotEqualTo(saved.getId());
        assertThat(type.getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    void deleteById_withNonExistentId_shouldNotDeleteAnything() {
        // given
        final UUID anotherId = UUID.randomUUID();
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteById(anotherId);

        // then
        assertThat(underTest.findAll()).hasSize(2);
    }

    @Test
    void deleteAll_shouldDeleteAllEntities() {
        // given
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.deleteAll();

        // then
        assertThat(underTest.findAll()).isEmpty();
    }

    @Test
    void create_withNonExistentName_shouldCreateEntity() {
        // given
        final String anotherName = "Пасмурно";
        final var newType = WeatherType.builder()
                .name(anotherName)
                .build();
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.create(newType);

        // then
        final var types = underTest.findAll();
        assertThat(types).hasSize(3);
        final var type = types.get(2);
        assertThat(type).isNotNull();
        assertThat(type.getId())
                .isNotNull();
        assertThat(type.getName())
                .isEqualTo(anotherName);
    }

    @Test
    void create_withExistentName_shouldThrowException() {
        // given
        final var savedBlizzard = underTest.create(typeBlizzard);
        final var savedClear = underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        // then
        assertThatThrownBy(() -> underTest.create(typeClear))
                .isInstanceOf(DuplicateKeyException.class);

        final var types = underTest.findAll();
        assertThat(types).hasSize(2);
        final var blizzard = types.get(0);
        assertThat(blizzard).isNotNull();
        assertThat(blizzard.getId())
                .isEqualTo(savedBlizzard.getId());
        assertThat(blizzard.getName())
                .isEqualTo(typeBlizzard.getName());
        final var clear = types.get(1);
        assertThat(clear).isNotNull();
        assertThat(clear.getId())
                .isEqualTo(savedClear.getId());
        assertThat(clear.getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    void change_withNonExistentName_shouldUpdateEntity() {
        // given
        final String anotherName = "Пасмурно";
        final var newType = WeatherType.builder()
                .name(anotherName)
                .build();
        final var saved = underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        underTest.change(saved.getId(), newType);

        // then
        final var types = underTest.findAll();
        assertThat(types).hasSize(2);
        final var type = types.get(0);
        assertThat(type).isNotNull();
        assertThat(type.getId())
                .isEqualTo(saved.getId());
        assertThat(type.getName())
                .isEqualTo(anotherName);
    }

    @Test
    void change_withExistentName_shouldThrowException() {
        // given
        final var savedBlizzard = underTest.create(typeBlizzard);
        final var savedClear = underTest.create(typeClear);
        final UUID blizzardId = savedBlizzard.getId();
        assertThat(underTest.findAll()).hasSize(2);

        // when
        // then
        assertThatThrownBy(() -> underTest.change(blizzardId, typeClear))
                .isInstanceOf(DuplicateKeyException.class);

        final var types = underTest.findAll();
        assertThat(types).hasSize(2);
        final var blizzard = types.get(0);
        assertThat(blizzard).isNotNull();
        assertThat(blizzard.getId())
                .isEqualTo(savedBlizzard.getId());
        assertThat(blizzard.getName())
                .isEqualTo(typeBlizzard.getName());
        final var clear = types.get(1);
        assertThat(clear).isNotNull();
        assertThat(clear.getId())
                .isEqualTo(savedClear.getId());
        assertThat(clear.getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    void findFirstByName_withExistentName_shouldReturnFoundEntity() {
        // given
        final var saved = underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByName(typeBlizzard.getName());

        // then
        assertThat(optType).isPresent();
        assertThat(optType.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optType.get().getName())
                .isEqualTo(typeBlizzard.getName());
    }

    @Test
    void findFirstByName_withNonExistentName_shouldReturnEmptyOptional() {
        // given
        final String anotherName = "Пасмурно";
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByName(anotherName);

        // then
        assertThat(optType).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withExistentNameAndNonExistentId_shouldReturnFoundEntity() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final var saved = underTest.create(typeClear);
        underTest.create(typeBlizzard);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameWithAnotherId(
                typeClear.getName(),
                anotherId
        );

        // then
        assertThat(optType).isPresent();
        assertThat(optType.get().getId())
                .isEqualTo(saved.getId());
        assertThat(optType.get().getName())
                .isEqualTo(typeClear.getName());
    }

    @Test
    void findFirstByNameAndIdNot_withExistentNameAndExistentId_shouldReturnEmptyOptional() {
        // given
        final var saved = underTest.create(typeClear);
        underTest.create(typeBlizzard);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameWithAnotherId(
                typeClear.getName(),
                saved.getId()
        );

        // then
        assertThat(optType).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withNonExistentNameAndNonExistentId_shouldReturnEmptyOptional() {
        // given
        final UUID anotherId = UUID.randomUUID();
        final String anotherName = "Пасмурно";
        underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameWithAnotherId(
                anotherName,
                anotherId
        );

        // then
        assertThat(optType).isNotPresent();
    }

    @Test
    void findFirstByNameAndIdNot_withNonExistentNameAndExistentId_shouldReturnEmptyOptional() {
        // given
        final String anotherName = "Пасмурно";
        final var saved = underTest.create(typeBlizzard);
        underTest.create(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameWithAnotherId(
                anotherName,
                saved.getId()
        );

        // then
        assertThat(optType).isNotPresent();
    }
}