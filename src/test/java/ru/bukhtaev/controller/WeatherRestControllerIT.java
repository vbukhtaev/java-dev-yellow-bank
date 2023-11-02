package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.util.Accuracy;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
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
class WeatherRestControllerIT extends AbstractIntegrationTest {

    /**
     * URL.
     */
    private static final String API_V1_WEATHER_DATA = "/api/v1/weather-data";

    /**
     * Маппер для DTO данных о погоде.
     */
    @Autowired
    private IWeatherMapper weatherMapper;

    /**
     * Репозиторий данных о погоде.
     */
    @Autowired
    private IWeatherJpaRepository weatherRepository;

    /**
     * Репозиторий городов.
     */
    @Autowired
    private ICityJpaRepository cityRepository;

    /**
     * Репозиторий типов погоды.
     */
    @Autowired
    private IWeatherTypeJpaRepository typeRepository;

    private WeatherRequestDto weather1;
    private WeatherRequestDto weather2;

    private City cityKazan;
    private City cityYekaterinburg;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    void setUp() {
        cityKazan = cityRepository.save(
                City.builder()
                        .name("Казань")
                        .build()
        );
        cityYekaterinburg = cityRepository.save(
                City.builder()
                        .name("Екатеринбург")
                        .build()
        );
        typeClear = typeRepository.save(
                WeatherType.builder()
                        .name("Ясно")
                        .build()
        );
        typeBlizzard = typeRepository.save(
                WeatherType.builder()
                        .name("Метель")
                        .build()
        );

        weather1 = WeatherRequestDto.builder()
                .cityId(cityKazan.getId())
                .typeId(typeClear.getId())
                .temperature(24.57)
                .dateTime(NOW)
                .build();
        weather2 = WeatherRequestDto.builder()
                .cityId(cityYekaterinburg.getId())
                .typeId(typeBlizzard.getId())
                .temperature(-28.72)
                .dateTime(YESTERDAY)
                .build();
    }

    @AfterEach
    void tearDown() {
        weatherRepository.deleteAll();
        typeRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Test
    void getAll_shouldReturnAllEntities() throws Exception {
        // given
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather1));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final var requestBuilder = get(API_V1_WEATHER_DATA);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(saved.getId().toString())),
                        jsonPath("$[0].city.id", is(cityKazan.getId().toString())),
                        jsonPath("$[0].city.name", is(cityKazan.getName())),
                        jsonPath("$[0].type.id", is(typeClear.getId().toString())),
                        jsonPath("$[0].type.name", is(typeClear.getName())),
                        jsonPath("$[0].temperature", is(weather1.getTemperature())),
                        jsonPath("$[0].dateTime",
                                is(weather1.getDateTime().format(DATE_TIME_FORMATTER)))
                );
    }

    @Test
    void getById_withExistentId_shouldReturnFoundEntity() throws Exception {
        // given
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather1));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final var requestBuilder = get(API_V1_WEATHER_DATA + "/{id}", saved.getId());

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(saved.getId().toString())),
                        jsonPath("$.city.id", is(cityKazan.getId().toString())),
                        jsonPath("$.city.name", is(cityKazan.getName())),
                        jsonPath("$.type.id", is(typeClear.getId().toString())),
                        jsonPath("$.type.name", is(typeClear.getName())),
                        jsonPath("$.temperature", is(weather1.getTemperature())),
                        jsonPath("$.dateTime",
                                is(weather1.getDateTime().format(DATE_TIME_FORMATTER)))
                );
    }

    @Test
    void getById_withNonExistentId_shouldReturnError() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final UUID nonExistentId = UUID.randomUUID();
        final var requestBuilder = get(API_V1_WEATHER_DATA + "/{id}", nonExistentId);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Weather with ID = <{0}> not found!",
                                        nonExistentId
                                )
                        ))
                );
    }

    @Test
    void create_withNonExistentCityAndDateTime_shouldReturnCreatedEntity() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather2));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(weather1);
        var requestBuilder = post(API_V1_WEATHER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", notNullValue()),
                        jsonPath("$.city.id", is(cityKazan.getId().toString())),
                        jsonPath("$.city.name", is(cityKazan.getName())),
                        jsonPath("$.type.id", is(typeClear.getId().toString())),
                        jsonPath("$.type.name", is(typeClear.getName())),
                        jsonPath("$.temperature", is(weather1.getTemperature())),
                        jsonPath("$.dateTime",
                                is(weather1.getDateTime().format(DATE_TIME_FORMATTER)))
                );

        final List<Weather> weatherData = weatherRepository.findAll();
        assertThat(weatherData).hasSize(2);
        final Weather weather = weatherData.get(1);
        assertThat(weather.getId()).isNotNull();
        assertThat(weather.getCity().getId()).isEqualTo(cityKazan.getId());
        assertThat(weather.getCity().getName()).isEqualTo(cityKazan.getName());
        assertThat(weather.getType().getId()).isEqualTo(typeClear.getId());
        assertThat(weather.getType().getName()).isEqualTo(typeClear.getName());
        assertThat(weather.getTemperature()).isEqualTo(weather1.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(weather1.getDateTime());
    }

    @Test
    void create_withExistentCityAndDateTime_shouldReturnError() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final String jsonRequest = objectMapper.writeValueAsString(weather1);
        var requestBuilder = post(API_V1_WEATHER_DATA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("city", "dateTime")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                                        cityKazan.getId(),
                                        weather1.getDateTime().format(DATE_TIME_FORMATTER)
                                )
                        ))
                );

        assertThat(weatherRepository.findAll()).hasSize(1);
    }

    @Test
    void replace_withNonExistentCityAndDateTime_shouldReturnReplacedEntity() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather2));
        assertThat(weatherRepository.findAll()).hasSize(2);
        final WeatherRequestDto dto = WeatherRequestDto.builder()
                .cityId(weather1.getCityId())
                .typeId(weather1.getTypeId())
                .temperature(weather1.getTemperature())
                .dateTime(weather2.getDateTime())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        var requestBuilder = put(API_V1_WEATHER_DATA + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(saved.getId().toString())),
                        jsonPath("$.city.id", is(cityKazan.getId().toString())),
                        jsonPath("$.city.name", is(cityKazan.getName())),
                        jsonPath("$.type.id", is(typeClear.getId().toString())),
                        jsonPath("$.type.name", is(typeClear.getName())),
                        jsonPath("$.temperature", is(weather1.getTemperature())),
                        jsonPath("$.dateTime",
                                is(weather2.getDateTime().format(DATE_TIME_FORMATTER)))
                );

        final List<Weather> weatherData = weatherRepository.findAll();
        assertThat(weatherData).hasSize(2);
        final Weather weather = weatherData.get(1);
        assertThat(weather.getId()).isEqualTo(saved.getId());
        assertThat(weather.getCity().getId()).isEqualTo(cityKazan.getId());
        assertThat(weather.getCity().getName()).isEqualTo(cityKazan.getName());
        assertThat(weather.getType().getId()).isEqualTo(typeClear.getId());
        assertThat(weather.getType().getName()).isEqualTo(typeClear.getName());
        assertThat(weather.getTemperature()).isEqualTo(weather1.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(weather2.getDateTime());
    }

    @Test
    void replace_withExistentCityAndDateTime_shouldReturnError() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather2));
        assertThat(weatherRepository.findAll()).hasSize(2);
        final WeatherRequestDto dto = WeatherRequestDto.builder()
                .cityId(weather1.getCityId())
                .typeId(weather2.getTypeId())
                .temperature(weather2.getTemperature())
                .dateTime(weather1.getDateTime())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        var requestBuilder = put(API_V1_WEATHER_DATA + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("city", "dateTime")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                                        cityKazan.getId(),
                                        weather1.getDateTime().format(DATE_TIME_FORMATTER)
                                )
                        ))
                );

        final Optional<Weather> optWeather = weatherRepository.findById(saved.getId());
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getCity().getId()).isEqualTo(cityYekaterinburg.getId());
        assertThat(optWeather.get().getCity().getName()).isEqualTo(cityYekaterinburg.getName());
        assertThat(optWeather.get().getType().getId()).isEqualTo(typeBlizzard.getId());
        assertThat(optWeather.get().getType().getName()).isEqualTo(typeBlizzard.getName());
        assertThat(optWeather.get().getTemperature()).isEqualTo(weather2.getTemperature());
        assertThat(optWeather.get().getDateTime()).isEqualTo(weather2.getDateTime());
    }

    @Test
    void update_withNonExistentCityAndDateTime_shouldReturnUpdatedEntity() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather2));
        assertThat(weatherRepository.findAll()).hasSize(2);
        final WeatherRequestDto dto = WeatherRequestDto.builder()
                .cityId(weather1.getCityId())
                .typeId(weather1.getTypeId())
                .temperature(weather1.getTemperature())
                .dateTime(weather2.getDateTime())
                .build();
        final String jsonRequest = objectMapper.writeValueAsString(dto);
        var requestBuilder = patch(API_V1_WEATHER_DATA + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", is(saved.getId().toString())),
                        jsonPath("$.city.id", is(cityKazan.getId().toString())),
                        jsonPath("$.city.name", is(cityKazan.getName())),
                        jsonPath("$.type.id", is(typeClear.getId().toString())),
                        jsonPath("$.type.name", is(typeClear.getName())),
                        jsonPath("$.temperature", is(weather1.getTemperature())),
                        jsonPath("$.dateTime",
                                is(weather2.getDateTime().format(DATE_TIME_FORMATTER)))
                );

        final List<Weather> weatherData = weatherRepository.findAll();
        assertThat(weatherData).hasSize(2);
        final Weather weather = weatherData.get(1);
        assertThat(weather.getCity().getId()).isEqualTo(cityKazan.getId());
        assertThat(weather.getCity().getName()).isEqualTo(cityKazan.getName());
        assertThat(weather.getType().getId()).isEqualTo(typeClear.getId());
        assertThat(weather.getType().getName()).isEqualTo(typeClear.getName());
        assertThat(weather.getTemperature()).isEqualTo(weather1.getTemperature());
        assertThat(weather.getDateTime()).isEqualTo(weather2.getDateTime());
    }

    @Test
    void update_withExistentCityAndDateTime_shouldReturnError() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final Weather saved = weatherRepository.save(weatherMapper.convertFromDto(weather2));
        assertThat(weatherRepository.findAll()).hasSize(2);
        final WeatherRequestDto dto = WeatherRequestDto.builder()
                .cityId(weather1.getCityId())
                .typeId(weather2.getTypeId())
                .temperature(weather2.getTemperature())
                .dateTime(weather1.getDateTime())
                .build();

        final String jsonRequest = objectMapper.writeValueAsString(dto);
        var requestBuilder = patch(API_V1_WEATHER_DATA + "/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("city", "dateTime")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Weather in the city with ID = <{0}> for time <{1}> already exists!",
                                        cityKazan.getId(),
                                        weather1.getDateTime().format(DATE_TIME_FORMATTER)
                                )
                        ))
                );

        final Optional<Weather> optWeather = weatherRepository.findById(saved.getId());
        assertThat(optWeather).isPresent();
        assertThat(optWeather.get().getCity().getId()).isEqualTo(cityYekaterinburg.getId());
        assertThat(optWeather.get().getCity().getName()).isEqualTo(cityYekaterinburg.getName());
        assertThat(optWeather.get().getType().getId()).isEqualTo(typeBlizzard.getId());
        assertThat(optWeather.get().getType().getName()).isEqualTo(typeBlizzard.getName());
        assertThat(optWeather.get().getTemperature()).isEqualTo(weather2.getTemperature());
        assertThat(optWeather.get().getDateTime()).isEqualTo(weather2.getDateTime());
    }

    @Test
    void delete_shouldDeleteEntityAndReturnStatusNoContent() throws Exception {
        // given
        final UUID weather1Id = weatherRepository.save(weatherMapper.convertFromDto(weather1)).getId();
        assertThat(weatherRepository.findAll()).hasSize(1);
        var requestBuilder = delete(API_V1_WEATHER_DATA + "/{id}", weather1Id);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(weatherRepository.findAll()).isEmpty();
    }

    @Test
    void get_withExistentCityNameAndDateTime_shouldReturnTemperature() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final var requestBuilder = get(
                API_V1_WEATHER_DATA + "/current/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(weather1.getTemperature().toString())
                );
    }

    @Test
    void get_withNonExistentCityNameAndDateTime_shouldThrowException() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        final String anotherCityName = "Новосибирск";
        final var requestBuilder = get(
                API_V1_WEATHER_DATA + "/current/{city}",
                anotherCityName
        );

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        MESSAGE_TEMPERATURE_NOT_FOUND,
                                        anotherCityName
                                )
                        ))
                );
    }

    @Test
    void delete_shouldDeleteWeatherDataForSpecifiedCityName() throws Exception {
        // given
        weatherRepository.save(weatherMapper.convertFromDto(weather1));
        assertThat(weatherRepository.findAll()).hasSize(1);
        final var requestBuilder = delete(
                API_V1_WEATHER_DATA + "/for-city/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(weatherRepository.findAll()).isEmpty();
    }
}
