package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private IRepository<Weather> repository;

    private Weather weather1;
    private Weather weather2;
    private Weather weather3;
    private Weather weather4;

    @BeforeEach
    public void setUp() {
        final String cityA = "City A";
        final String cityB = "City B";
        final UUID cityIdA = UUID.fromString("9f96d8a7-1f66-4443-b083-b4c0b2291b7e");
        final UUID cityIdB = UUID.fromString("30dd69a3-0421-47f5-9387-f3083fe4b210");
        final LocalDateTime now = LocalDateTime.now();

        weather1 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(25.37).dateTime(now).build();
        weather2 = Weather.builder().cityId(cityIdA).cityName(cityA).temperature(-17.9).dateTime(now).build();
        weather3 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(24.7).dateTime(now).build();
        weather4 = Weather.builder().cityId(cityIdB).cityName(cityB).temperature(0.84).dateTime(now).build();

        repository.saveAll(List.of(
                weather1,
                weather2,
                weather3,
                weather4
        ));
    }

    @AfterEach
    void tearDown() {
        repository.clear();
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
    void getCitiesStrictlyWarmer() throws Exception {
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

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "9f96d8a7-1f66-4443-b083-b4c0b2291b7e": [25.37, -17.9],
                                    "30dd69a3-0421-47f5-9387-f3083fe4b210": [24.7, 0.84]
                                }
                                """)
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
                        jsonPath("$.['1'][0].cityId").value(weather4.getCityId().toString()),
                        jsonPath("$.['1'][0].cityName").value(weather4.getCityName()),
                        jsonPath("$.['1'][0].temperature").value(weather4.getTemperature()),
                        jsonPath("$.['1'][0].dateTime")
                                .value(containsString(weather4.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['-18'][0].cityId").value(weather2.getCityId().toString()),
                        jsonPath("$.['-18'][0].cityName").value(weather2.getCityName()),
                        jsonPath("$.['-18'][0].temperature").value(weather2.getTemperature()),
                        jsonPath("$.['-18'][0].dateTime")
                                .value(containsString(weather2.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['25'][0].cityId").value(weather1.getCityId().toString()),
                        jsonPath("$.['25'][0].cityName").value(weather1.getCityName()),
                        jsonPath("$.['25'][0].temperature").value(weather1.getTemperature()),
                        jsonPath("$.['25'][0].dateTime")
                                .value(containsString(weather1.getDateTime().format(DATE_TIME_FORMATTER))),
                        jsonPath("$.['25'][1].cityId").value(weather3.getCityId().toString()),
                        jsonPath("$.['25'][1].cityName").value(weather3.getCityName()),
                        jsonPath("$.['25'][1].temperature").value(weather3.getTemperature()),
                        jsonPath("$.['25'][1].dateTime")
                                .value(containsString(weather3.getDateTime().format(DATE_TIME_FORMATTER)))
                );
    }
}