package ru.bukhtaev.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.service.GenerationService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static ru.bukhtaev.TestUtils.createTestData;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Тестовый класс для сервиса {@link GenerationServiceImpl}
 */
class GenerationServiceImplTest extends AbstractServiceTest {

    /**
     * Регионы для генерации.
     */
    private static final Map<UUID, String> REGIONS = Map.of(
            UUID.randomUUID(), "Region A",
            UUID.randomUUID(), "Region B",
            UUID.randomUUID(), "Region C"
    );

    @Mock
    private Repository<Weather> repository;

    /**
     * Тестируемый сервис генерации данных о погоде.
     */
    private GenerationService underTest;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;

    @BeforeEach
    void setUp() {
        underTest = new GenerationServiceImpl(repository);

        weather1 = createTestData(REGIONS, 28.57);
        weather2 = createTestData(REGIONS, -5.68);
        weather3 = createTestData(REGIONS, 0.83);
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
    }

    @Test
    void generate_withValidParams_shouldGenerateData() {
        // given
        final int count = 16;

        // when
        underTest.generate(new HashSet<>(REGIONS.values()), count);

        // then
        verify(repository, times(1)).saveAll(anyList());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void generate_withEmptyRegionsSet_shouldThrowException() {
        // given
        final Set<String> regions = new HashSet<>();
        final int count = 5;

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.generate(regions, count),
                "Empty regions set!"
        );
        verifyNoInteractions(repository);
    }

    @Test
    void generate_withInvalidCount_shouldThrowException() {
        // given
        final int count = 0;
        final Set<String> regions = new HashSet<>(REGIONS.values());

        // when
        // then
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.generate(regions, count),
                "Incorrect count: " + count
        );
        verifyNoInteractions(repository);
    }

    @Test
    void print_shouldPrintData() {
        // given
        final List<Weather> testData = List.of(
                weather1,
                weather2,
                weather3
        );
        given(repository.findAll()).willReturn(testData);


        // when
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        underTest.print();

        // then
        final String line = "-".repeat(109) + "\n";
        final StringBuilder builder = new StringBuilder();
        builder.append("\n").append(line);
        builder.append(String.format("| %36s | %20s | %20s | %20s |",
                "ID",
                "Region",
                "Temperature",
                "Date and time"
        ));
        builder.append("\n").append(line);

        testData.forEach(weather -> {
            builder.append(
                    String.format("| %36s | %20s | %20.15f | %20s |",
                            weather.getRegionId(),
                            weather.getRegionName(),
                            weather.getTemperature(),
                            weather.getDateTime().format(DATE_TIME_FORMATTER)
                    )
            );
            builder.append("\n");
        });
        builder.append(line);

        assertEquals(builder.toString(), outContent.toString());
    }

    @Test
    void print_withNoData_shouldNotPrintData() {
        // given
        given(repository.findAll()).willReturn(Collections.emptyList());

        // when
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        underTest.print();

        // then
        assertEquals("", outContent.toString());
    }

    @Test
    void clear_shouldCallClearMethodOfRepository() {
        // when
        underTest.clear();

        // then
        verify(repository, times(1)).clear();
    }
}
