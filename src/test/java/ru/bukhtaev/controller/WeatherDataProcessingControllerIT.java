package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Интеграционные тесты для обработки данных о погоде.
 */
class WeatherDataProcessingControllerIT extends AbstractIntegrationTest {

    /**
     * Репозиторий для работы с данными о погоде.
     */
    @Autowired
    private IWeatherJpaRepository weatherRepository;

    /**
     * Репозиторий для работы с типами погоды.
     */
    @Autowired
    private IWeatherTypeJpaRepository typeRepository;

    /**
     * Репозиторий для работы с городами.
     */
    @Autowired
    private ICityJpaRepository cityRepository;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;
    private Weather weather4;

    private City cityA;
    private City cityB;

    @BeforeEach
    public void setUp() {
        cityA = cityRepository.save(
                City.builder()
                        .name("City A")
                        .build()
        );
        cityB = cityRepository.save(
                City.builder()
                        .name("City B")
                        .build()
        );

        final WeatherType typeA = typeRepository.save(
                WeatherType.builder()
                        .name("Type A")
                        .build()
        );
        final WeatherType typeB = typeRepository.save(
                WeatherType.builder()
                        .name("Type B")
                        .build()
        );

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        weather1 = Weather.builder().city(cityA).type(typeA).temperature(25.37).dateTime(now).build();
        weather2 = Weather.builder().city(cityA).type(typeA).temperature(-17.9).dateTime(yesterday).build();
        weather3 = Weather.builder().city(cityB).type(typeB).temperature(24.7).dateTime(now).build();
        weather4 = Weather.builder().city(cityB).type(typeB).temperature(0.84).dateTime(yesterday).build();

        weatherRepository.saveAll(List.of(
                weather1,
                weather2,
                weather3,
                weather4
        ));
    }

    @AfterEach
    void tearDown() {
        weatherRepository.deleteAll();
        typeRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Test
    void getAverageTemperature_shouldReturnGeneralAverageTemperature() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get("/api/weather/processing/average-temperature");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(Double.valueOf(8.253).toString())
                );
    }

    @Test
    void getAverageTemperatures_shouldReturnAverageTemperatureForEveryCity() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get("/api/weather/processing/average-temperatures");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "City A": 3.735,
                                    "City B": 12.77
                                }
                                """)
                );
    }

    @Test
    void getCitiesWarmer_shouldReturnCitiesWarmerThatSpecifiedTemperature() throws Exception {
        // given
        final double temperature = 20.1;
        final var requestBuilder = get("/api/weather/processing/cities-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    "City A",
                                    "City B"
                                ]
                                """)
                );
    }

    @Test
    void getCitiesStrictlyWarmer_shouldReturnCitiesStrictlyWarmerThatSpecifiedTemperature() throws Exception {
        // given
        final double temperature = 0.0;
        final var requestBuilder = get("/api/weather/processing/cities-strictly-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                   "City B"
                                ]
                                """)
                );
    }

    @Test
    void groupTemperaturesById() throws Exception {
        // given
        final var requestBuilder = get("/api/weather/processing/grouped-by-id");
        final String resultJson = """
                {
                    "cityIdA": [25.37, -17.9],
                    "cityIdB": [24.7, 0.84]
                }
                """
                .replace("cityIdA", cityA.getId().toString())
                .replace("cityIdB", cityB.getId().toString());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$").isMap(),
                        jsonPath("$", aMapWithSize(2)),
                        content().json(resultJson)
                );
    }

    @Test
    void groupByTemperature() throws Exception {
        // given
        final var requestBuilder = get("/api/weather/processing/grouped-by-temperature");

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$").isMap(),
                        jsonPath("$", aMapWithSize(3)),
                        jsonPath("$.['1'][0].city.id").value(weather4.getCity().getId().toString()),
                        jsonPath("$.['1'][0].city.name").value(weather4.getCity().getName()),
                        jsonPath("$.['1'][0].type.id").value(weather4.getType().getId().toString()),
                        jsonPath("$.['1'][0].type.name").value(weather4.getType().getName()),
                        jsonPath("$.['1'][0].temperature").value(weather4.getTemperature()),
                        jsonPath("$.['1'][0].dateTime")
                                .value(containsString(weather4.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['-18'][0].city.id").value(weather2.getCity().getId().toString()),
                        jsonPath("$.['-18'][0].city.name").value(weather2.getCity().getName()),
                        jsonPath("$.['-18'][0].type.id").value(weather2.getType().getId().toString()),
                        jsonPath("$.['-18'][0].type.name").value(weather2.getType().getName()),
                        jsonPath("$.['-18'][0].temperature").value(weather2.getTemperature()),
                        jsonPath("$.['-18'][0].dateTime")
                                .value(containsString(weather2.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['25'][0].city.id").value(weather1.getCity().getId().toString()),
                        jsonPath("$.['25'][0].city.name").value(weather1.getCity().getName()),
                        jsonPath("$.['25'][0].type.id").value(weather1.getType().getId().toString()),
                        jsonPath("$.['25'][0].type.name").value(weather1.getType().getName()),
                        jsonPath("$.['25'][0].temperature").value(weather1.getTemperature()),
                        jsonPath("$.['25'][0].dateTime")
                                .value(containsString(weather1.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['25'][1].city.id").value(weather3.getCity().getId().toString()),
                        jsonPath("$.['25'][1].city.name").value(weather3.getCity().getName()),
                        jsonPath("$.['25'][1].type.id").value(weather3.getType().getId().toString()),
                        jsonPath("$.['25'][1].type.name").value(weather3.getType().getName()),
                        jsonPath("$.['25'][1].temperature").value(weather3.getTemperature()),
                        jsonPath("$.['25'][1].dateTime")
                                .value(containsString(weather3.getDateTime().format(DATE_TIME_FORMATTER)))
                );
    }
}