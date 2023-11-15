package ru.bukhtaev.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.util.Accuracy;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.TestUtils.MESSAGE_TEMPERATURE_NOT_FOUND;
import static ru.bukhtaev.controller.WeatherDataProcessingController.URL_API_WEATHER_PROCESSING;
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

    private City cityKazan;
    private City cityYekaterinburg;

    private WeatherType typeClear;
    private WeatherType typeBlizzard;

    @BeforeEach
    public void setUp() {
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

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        weather1 = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(25.37)
                .dateTime(now)
                .build();
        weather2 = Weather.builder()
                .city(cityKazan)
                .type(typeClear)
                .temperature(-17.9)
                .dateTime(yesterday)
                .build();
        weather3 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeBlizzard)
                .temperature(24.7)
                .dateTime(now)
                .build();
        weather4 = Weather.builder()
                .city(cityYekaterinburg)
                .type(typeBlizzard)
                .temperature(0.84)
                .dateTime(yesterday)
                .build();

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
    @WithMockUser(authorities = "weather-data:read")
    void getAverageTemperature_shouldReturnGeneralAverageTemperature() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperature");

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
    @WithMockUser
    void getAverageTemperature_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperature");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAverageTemperature_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperature");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void getAverageTemperatures_shouldReturnAverageTemperatureForEveryCity() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperatures");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "Казань": 3.735,
                                    "Екатеринбург": 12.77
                                }
                                """)
                );
    }

    @Test
    @WithMockUser
    void getAverageTemperatures_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperatures");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getAverageTemperatures_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final int precision = 3;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/average-temperatures");

        // when
        mockMvc.perform(requestBuilder.param("precision", Integer.toString(precision)))

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void getCitiesWarmer_shouldReturnCitiesWarmerThatSpecifiedTemperature() throws Exception {
        // given
        final double temperature = 20.1;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    "Казань",
                                    "Екатеринбург"
                                ]
                                """)
                );
    }

    @Test
    @WithMockUser
    void getCitiesWarmer_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final double temperature = 20.1;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getCitiesWarmer_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final double temperature = 20.1;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void getCitiesStrictlyWarmer_shouldReturnCitiesStrictlyWarmerThatSpecifiedTemperature() throws Exception {
        // given
        final double temperature = 0.0;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-strictly-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                   "Екатеринбург"
                                ]
                                """)
                );
    }

    @Test
    @WithMockUser
    void getCitiesStrictlyWarmer_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final double temperature = 0.0;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-strictly-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getCitiesStrictlyWarmer_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final double temperature = 0.0;
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/cities-strictly-warmer");

        // when
        mockMvc.perform(requestBuilder.param("temperature", Double.toString(temperature)))

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void groupTemperaturesById_shouldReturnTemperaturesGroupedByCityId() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-id");
        final String resultJson = """
                {
                    "cityKazanId": [25.37, -17.9],
                    "cityYekaterinburgId": [24.7, 0.84]
                }
                """
                .replace("cityKazanId", cityKazan.getId().toString())
                .replace("cityYekaterinburgId", cityYekaterinburg.getId().toString());

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
    @WithMockUser
    void groupTemperaturesById_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-id");

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void groupTemperaturesById_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-id");

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void groupByTemperature_shouldReturnWeatherDataGroupedByTemperature() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-temperature");

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

    @Test
    @WithMockUser
    void groupByTemperature_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-temperature");

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void groupByTemperature_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        final var requestBuilder = get(URL_API_WEATHER_PROCESSING + "/grouped-by-temperature");

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void get_withExistentCityNameAndDateTime_shouldReturnTemperature() throws Exception {
        // given
        weatherRepository.save(weather1);
        final var requestBuilder = get(
                URL_API_WEATHER_PROCESSING + "/current/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.HOURS.name()))

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(weather1.getTemperature().toString())
                );
    }

    @Test
    @WithMockUser
    void get_withoutReadAuthority_accessShouldBeDenied() throws Exception {
        // given
        weatherRepository.save(weather1);
        final var requestBuilder = get(
                URL_API_WEATHER_PROCESSING + "/current/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void get_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        weatherRepository.save(weather1);
        final var requestBuilder = get(
                URL_API_WEATHER_PROCESSING + "/current/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder.param("accuracy", Accuracy.MINUTES.name()))

                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void get_withNonExistentCityNameAndDateTime_shouldThrowException() throws Exception {
        // given
        weatherRepository.save(weather1);
        final String anotherCityName = "Новосибирск";
        final var requestBuilder = get(
                URL_API_WEATHER_PROCESSING + "/current/{city}",
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
    @WithMockUser(authorities = "weather-data:write")
    void deleteForCity_shouldDeleteWeatherDataForSpecifiedCityName() throws Exception {
        // given
        weatherRepository.save(weather1);
        assertThat(weatherRepository.findAll()).hasSize(4);
        final var requestBuilder = delete(
                URL_API_WEATHER_PROCESSING + "/for-city/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isNoContent());

        assertThat(weatherRepository.findAll()).hasSize(2);
    }

    @Test
    @WithMockUser(authorities = "weather-data:read")
    void deleteForCity_withoutWriteAuthority_accessShouldBeDenied() throws Exception {
        // given
        weatherRepository.save(weather1);
        assertThat(weatherRepository.findAll()).hasSize(4);
        final var requestBuilder = delete(
                URL_API_WEATHER_PROCESSING + "/for-city/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isForbidden());

        assertThat(weatherRepository.findAll()).hasSize(4);
    }

    @Test
    @WithAnonymousUser
    void deleteForCity_withoutAuthentication_accessShouldBeDenied() throws Exception {
        // given
        weatherRepository.save(weather1);
        assertThat(weatherRepository.findAll()).hasSize(4);
        final var requestBuilder = delete(
                URL_API_WEATHER_PROCESSING + "/for-city/{city}",
                cityKazan.getName()
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpect(status().isUnauthorized());

        assertThat(weatherRepository.findAll()).hasSize(4);
    }
}