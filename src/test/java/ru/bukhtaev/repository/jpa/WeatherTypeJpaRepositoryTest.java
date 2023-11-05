package ru.bukhtaev.repository.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.bukhtaev.AbstractContainerizedTest;
import ru.bukhtaev.model.WeatherType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест JPA-репозитория типов погоды.
 */
@DataJpaTest
class WeatherTypeJpaRepositoryTest extends AbstractContainerizedTest {

    /**
     * Тестируемый JPA-репозиторий типов погоды.
     */
    @Autowired
    private IWeatherTypeJpaRepository underTest;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
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
    void findFirstByName_withExistentName_shouldReturnFoundEntity() {
        // given
        final var saved = underTest.save(typeBlizzard);
        underTest.save(typeClear);
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
        underTest.save(typeBlizzard);
        underTest.save(typeClear);
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
        final var saved = underTest.save(typeClear);
        underTest.save(typeBlizzard);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameAndIdNot(
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
        final var saved = underTest.save(typeClear);
        underTest.save(typeBlizzard);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameAndIdNot(
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
        underTest.save(typeBlizzard);
        underTest.save(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameAndIdNot(
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
        final var saved = underTest.save(typeBlizzard);
        underTest.save(typeClear);
        assertThat(underTest.findAll()).hasSize(2);

        // when
        final var optType = underTest.findFirstByNameAndIdNot(
                anotherName,
                saved.getId()
        );

        // then
        assertThat(optType).isNotPresent();
    }
}