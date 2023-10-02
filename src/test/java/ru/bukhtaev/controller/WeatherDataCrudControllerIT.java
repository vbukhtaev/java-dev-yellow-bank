package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;
import ru.bukhtaev.util.Accuracy;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.TestUtils.MESSAGE_TEMPERATURE_NOT_FOUND;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Интеграционные тесты для CRUD операций над данными о погоде.
 */
class WeatherDataCrudControllerIT extends AbstractIntegrationTest {

    /**
     * Репозиторий для работы с данными о погоде.
     */
    @Autowired
    private IRepository<Weather> repository;

    private WeatherRequestDto requestDto;

    @BeforeEach
    void setUp() {
        final Double temperature = -28.7;
        final LocalDateTime now = LocalDateTime.now();

        requestDto = new WeatherRequestDto(temperature, now);
    }

    @AfterEach
    void tearDown() {
        repository.clear();
    }

    @Test
    void get_withExistentCityNameAndDateTime_shouldReturnTemperature() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -28.7;
        final LocalDateTime now = LocalDateTime.now();

        final Weather weather = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(now)
                .build();
        repository.save(weather);
        final var requestBuilder = get("/api/weather/{city}", cityName);

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(temperature.toString())
                );
    }

    @Test
    void get_withNonExistentCityNameAndDateTime_shouldThrowException() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -28.7;
        final LocalDateTime now = LocalDateTime.now();

        final Weather weather = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(now)
                .build();
        repository.save(weather);

        final String anotherCityName = "Another city";
        final var requestBuilder = get("/api/weather/{city}", anotherCityName);

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(MESSAGE_TEMPERATURE_NOT_FOUND, anotherCityName)
                        ))
                );
    }

    @Test
    void create_withNonExistentCityName_shouldCreateWeatherDataWithNewCityId() throws Exception {
        // given
        final String cityName = "City N";
        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final var requestBuilder = post("/api/weather/{city}", cityName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.cityId", notNullValue()),
                        jsonPath("$.cityName", is(cityName)),
                        jsonPath("$.temperature", is(requestDto.getTemperature())),
                        jsonPath("$.dateTime",
                                containsString(requestDto.getDateTime().format(DATE_TIME_FORMATTER))
                        )
                );

        final List<Weather> data = repository.findAll();
        assertThat(data).hasSize(1);
        final Weather weather = data.get(0);
        assertThat(weather.getCityId()).isNotNull();
        assertThat(weather.getCityName()).isEqualTo(cityName);
        assertThat(weather.getTemperature()).isEqualTo(requestDto.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(requestDto.getDateTime());
    }

    @Test
    void create_withExistentCityName_shouldCreateWeatherDataWithExistentCityId() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -15.04;
        final LocalDateTime now = LocalDateTime.now();

        final Weather existent = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(now)
                .build();
        repository.save(existent);

        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final var requestBuilder = post("/api/weather/{city}", cityName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.cityId", notNullValue()),
                        jsonPath("$.cityName", is(cityName)),
                        jsonPath("$.temperature", is(requestDto.getTemperature())),
                        jsonPath("$.dateTime",
                                containsString(requestDto.getDateTime().format(DATE_TIME_FORMATTER))
                        )
                );

        final List<Weather> data = repository.findAll();
        assertThat(data).hasSize(2);
        final Weather weather = data.get(1);
        assertThat(weather.getCityId()).isEqualTo(existent.getCityId());
        assertThat(weather.getCityName()).isEqualTo(cityName);
        assertThat(weather.getTemperature()).isEqualTo(requestDto.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(requestDto.getDateTime());
    }

    @Test
    void update_withExistentData_shouldUpdateExistentWeatherData() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -15.04;
        final LocalDateTime dateTime = requestDto.getDateTime();

        final Weather existent = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(dateTime)
                .build();
        repository.save(existent);

        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final var requestBuilder = put("/api/weather/{city}", cityName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.cityId", is(cityId.toString())),
                        jsonPath("$.cityName", is(cityName)),
                        jsonPath("$.temperature", is(requestDto.getTemperature())),
                        jsonPath("$.dateTime", containsString(dateTime.format(DATE_TIME_FORMATTER)))
                );

        final List<Weather> data = repository.findAll();
        assertThat(data).hasSize(1);
        final Weather weather = data.get(0);
        assertThat(weather.getCityId()).isEqualTo(cityId);
        assertThat(weather.getCityName()).isEqualTo(cityName);
        assertThat(weather.getTemperature()).isEqualTo(requestDto.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    void update_withNotMatchingCityName_shouldCreateNewWeatherData() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -15.04;
        final LocalDateTime dateTime = requestDto.getDateTime();

        final Weather existent = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(dateTime)
                .build();
        repository.save(existent);

        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final String differentCityName = "Different";
        final var requestBuilder = put("/api/weather/{city}", differentCityName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.cityId", notNullValue()),
                        jsonPath("$.cityId", not(cityId.toString())),
                        jsonPath("$.cityName", is(differentCityName)),
                        jsonPath("$.temperature", is(requestDto.getTemperature())),
                        jsonPath("$.dateTime",
                                containsString(requestDto.getDateTime().format(DATE_TIME_FORMATTER))
                        )
                );

        final List<Weather> data = repository.findAll();
        assertThat(data).hasSize(2);
        final Weather weather = data.get(1);
        assertThat(weather.getCityId()).isNotNull();
        assertThat(weather.getCityId()).isNotEqualTo(cityId);
        assertThat(weather.getCityName()).isEqualTo(differentCityName);
        assertThat(weather.getTemperature()).isEqualTo(requestDto.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(requestDto.getDateTime());
    }

    @Test
    void update_withNotMatchingDateTime_shouldCreateNewWeatherData() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -15.04;
        final LocalDateTime dateTime = requestDto.getDateTime().minusDays(1);

        final Weather existent = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(dateTime)
                .build();
        repository.save(existent);

        final String jsonRequest = objectMapper.writeValueAsString(requestDto);
        final var requestBuilder = put("/api/weather/{city}", cityName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.cityId", is(cityId.toString())),
                        jsonPath("$.cityName", is(cityName)),
                        jsonPath("$.temperature", is(requestDto.getTemperature())),
                        jsonPath("$.dateTime",
                                containsString(requestDto.getDateTime().format(DATE_TIME_FORMATTER))
                        )
                );

        final List<Weather> data = repository.findAll();
        assertThat(data).hasSize(2);
        final Weather weather = data.get(1);
        assertThat(weather.getCityId()).isEqualTo(cityId);
        assertThat(weather.getCityName()).isEqualTo(cityName);
        assertThat(weather.getTemperature()).isEqualTo(requestDto.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(requestDto.getDateTime());
    }

    @Test
    void delete_shouldDeleteSpecifiedWeatherData() throws Exception {
        // given
        final UUID cityId = UUID.randomUUID();
        final String cityName = "City N";
        final Double temperature = -28.7;
        final LocalDateTime now = LocalDateTime.now();

        final Weather existent = Weather.builder()
                .cityId(cityId)
                .cityName(cityName)
                .temperature(temperature)
                .dateTime(now)
                .build();
        repository.save(existent);
        assertThat(repository.findAll()).hasSize(1);

        final var requestBuilder = MockMvcRequestBuilders.delete("/api/weather/{city}", cityName);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(repository.findAll()).isEmpty();
    }
}