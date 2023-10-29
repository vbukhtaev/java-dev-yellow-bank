package ru.bukhtaev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.bukhtaev.config.external.ExternalApiConfigParams;
import ru.bukhtaev.dto.external.*;
import ru.bukhtaev.exception.CommonClientSideException;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.util.ErrorCode;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.bukhtaev.util.ErrorCode.*;
import static ru.bukhtaev.util.Utils.DATE_TIME_FORMATTER;

/**
 * Интеграционные тесты для взаимодействия с внешним API.
 */
class ExternalWeatherApiControllerIT extends AbstractIntegrationTest {

    /**
     * Форматтер для {@link LocalDateTime}.
     */
    public static final DateTimeFormatter LOCAL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Дата и время.
     */
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now()
            .withYear(2023)
            .withMonth(7)
            .withDayOfMonth(28)
            .withHour(11)
            .withMinute(45)
            .withSecond(0)
            .withNano(0);

    /**
     * Название параметра для передачи местоположения.
     */
    private static final String LOCATION_PARAM_NAME = "location";

    /**
     * Базовый URL.
     */
    public static final String PATH = "/api/external";

    /**
     * URL для получения текущей погоды.
     */
    private static final String URL_CURRENT = PATH + "/current";

    /**
     * URL для получения и сохранения текущей погоды.
     */
    private static final String URL_SAVE_CURRENT = PATH + "/save-current";

    /**
     * URL для получения текущей погоды с параметром местоположения.
     */
    private static final String URL_CURRENT_WITH_LOCATION =
            URL_CURRENT + "?" + LOCATION_PARAM_NAME + "={location}";

    /**
     * URL для получения и сохранения текущей погоды с параметром местоположения.
     */
    private static final String URL_SAVE_CURRENT_WITH_LOCATION =
            URL_SAVE_CURRENT + "?" + LOCATION_PARAM_NAME + "={location}";

    /**
     * Параметры конфигурации внешнего API.
     */
    @Autowired
    private ExternalApiConfigParams apiConfig;

    /**
     * Spy-клиент.
     * Не «стабаются» только те кейсы, которые приводят к исключению
     * по вине клиента (наследники {@link CommonClientSideException}).
     */
    @SpyBean
    @Qualifier("restTemplateWithLoggingErrors")
    private RestTemplate restTemplate;

    /**
     * Маппер объектов.
     */
    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * Репозиторий данных о погоде.
     */
    @Autowired
    private IWeatherJpaRepository weatherRepository;

    /**
     * Builder для URI;
     */
    private UriComponentsBuilder uriBuilder;

    private String locationName;
    private ExternalApiWeatherResponse weatherResponse;

    @BeforeEach
    void setUp() {
        final String typeClearName = "Clear";
        final String cityKazanName = "Kazan";

        uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        apiConfig.getBaseUrl() + apiConfig.getCurrent().getUrl())
                .queryParam(apiConfig.getTokenParamName(), apiConfig.getToken())
                .queryParam(apiConfig.getCurrent().getLocationParamName(), cityKazanName);

        final var current = Current.builder()
                .temperatureC(BigDecimal.valueOf(28.7))
                .condition(ConditionDto.builder()
                        .text(typeClearName)
                        .build())
                .build();

        final var location = LocationDto.builder()
                .name(cityKazanName)
                .localtime(LOCAL_DATE_TIME.format(LOCAL_TIME_FORMATTER))
                .build();

        locationName = cityKazanName;
        weatherResponse = ExternalApiWeatherResponse.builder()
                .current(current)
                .location(location)
                .build();
    }

    @AfterEach
    void tearDown() {
        weatherRepository.deleteAll();
        typeRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Test
    void get_withHappyPath_shouldReturnCurrentWeather() throws Exception {
        // given
        final String locationTime = weatherResponse.getLocation().getLocaltime();
        final BigDecimal temperature = weatherResponse.getCurrent().getTemperatureC();
        final String conditionText = weatherResponse.getCurrent().getCondition().getText();

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(ResponseEntity.ok(
                objectMapper.writeValueAsString(weatherResponse)
        ));

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.location.name", is(locationName)),
                        jsonPath("$.location.localtime", is(locationTime)),
                        jsonPath("$.current.temp_c", is(temperature.doubleValue())),
                        jsonPath("$.current.condition.text", is(conditionText))
                );
    }

    @Test
    void get_withoutLocation_shouldThrowClientSideException() throws Exception {
        // given
        // when
        final var requestBuilder = get(URL_CURRENT);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("location")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Necessary parameter <{0}> not provided!",
                                        LOCATION_PARAM_NAME
                                )
                        ))
                );
    }

    @Test
    void get_withNonExistentLocation_shouldThrowClientSideException() throws Exception {
        // given
        locationName = "GGGGGGGG";

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("location")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "No location found for value <{0}>",
                                        locationName
                                )
                        ))
                );
    }

    @Test
    void get_withoutApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOKEN_NOT_PROVIDED);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withExceededApiTokenLimit_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOKEN_LIMIT_EXCEEDED);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withDisabledApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(DISABLED_TOKEN);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withInvalidApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_TOKEN);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withNoAccessForApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(ACCESS_DENIED);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withInvalidUrl_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_URL);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withInvalidJson_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_JSON);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withTooManyLocations_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOO_MANY_LOCATIONS);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void get_withExternalApiError_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(EXTERNAL_API_ERROR);

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = get(
                URL_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withHappyPath_shouldSaveAndReturnCurrentWeather() throws Exception {
        // given
        final BigDecimal temperature = weatherResponse.getCurrent().getTemperatureC();
        final String conditionText = weatherResponse.getCurrent().getCondition().getText();
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(ResponseEntity.ok(
                objectMapper.writeValueAsString(weatherResponse)
        ));

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id", notNullValue()),
                        jsonPath("$.city.id", notNullValue()),
                        jsonPath("$.city.name", is(locationName)),
                        jsonPath("$.type.id", notNullValue()),
                        jsonPath("$.type.name", is(conditionText)),
                        jsonPath("$.temperature", is(temperature.doubleValue())),
                        jsonPath("$.dateTime",
                                is(LOCAL_DATE_TIME.format(DATE_TIME_FORMATTER)))
                );
    }

    @Test
    void getAndSave_withoutLocation_shouldThrowClientSideException() throws Exception {
        // given
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        // when
        final var requestBuilder = post(URL_SAVE_CURRENT);

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("location")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "Necessary parameter <{0}> not provided!",
                                        LOCATION_PARAM_NAME
                                )
                        ))
                );
    }

    @Test
    void getAndSave_withNonExistentLocation_shouldThrowClientSideException() throws Exception {
        // given
        locationName = "GGGGGGGG";
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].paramNames", contains("location")),
                        jsonPath("$.violations[0].message", is(
                                MessageFormat.format(
                                        "No location found for value <{0}>",
                                        locationName
                                )
                        ))
                );
    }

    @Test
    void getAndSave_withoutApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOKEN_NOT_PROVIDED);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withExceededApiTokenLimit_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOKEN_LIMIT_EXCEEDED);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withDisabledApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(DISABLED_TOKEN);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withInvalidApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_TOKEN);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withNoAccessForApiToken_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(ACCESS_DENIED);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withInvalidUrl_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_URL);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withInvalidJson_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(INVALID_JSON);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withTooManyLocations_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(TOO_MANY_LOCATIONS);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    @Test
    void getAndSave_withExternalApiError_shouldThrowServerSideException() throws Exception {
        // given
        final var errorResponse = getResponseWithCode(EXTERNAL_API_ERROR);
        uriBuilder.queryParam(
                apiConfig.getCurrent().getLanguageParamName(),
                Locale.ENGLISH.getLanguage()
        );

        given(restTemplate.getForEntity(
                uriBuilder.toUriString(),
                String.class
        )).willReturn(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(objectMapper.writeValueAsString(errorResponse))
        );

        // when
        final var requestBuilder = post(
                URL_SAVE_CURRENT_WITH_LOCATION,
                locationName
        );

        // when
        mockMvc.perform(requestBuilder)

                // then
                .andExpectAll(
                        status().is5xxServerError(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.violations", hasSize(1)),
                        jsonPath("$.violations[0].message",
                                is(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                );
    }

    /**
     * Возвращает объект типа {@link ExternalApiErrorResponse} с указанным кодом {@link ErrorCode}.
     *
     * @param code код {@link ErrorCode}
     * @return объект типа {@link ExternalApiErrorResponse} с указанным кодом {@link ErrorCode}
     */
    private ExternalApiErrorResponse getResponseWithCode(final ErrorCode code) {
        return ExternalApiErrorResponse.builder()
                .error(ApiErrorDto.builder()
                        .code(code.getCode())
                        .build()
                ).build();
    }
}