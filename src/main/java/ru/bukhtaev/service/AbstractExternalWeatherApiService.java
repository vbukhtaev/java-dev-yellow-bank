package ru.bukhtaev.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.bukhtaev.config.external.ExternalApiConfigParams;
import ru.bukhtaev.dto.external.ExternalApiErrorResponse;
import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.exception.CommonServerSideException;
import ru.bukhtaev.exception.external.*;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.util.ErrorCode;

import java.text.MessageFormat;

/**
 * Частичная реализация сервиса для выполнения запросов к внешнему API данных о погоде.
 */
@RateLimiter(name = "rateLimitedApi")
public abstract class AbstractExternalWeatherApiService implements IExternalWeatherApiService {

    /**
     * Название параметра для передачи местоположения.
     */
    protected static final String LOCATION_PARAM_NAME = "location";

    /**
     * Параметры конфигурации внешнего API.
     */
    protected final ExternalApiConfigParams apiConfig;

    /**
     * Клиент.
     */
    protected final RestTemplate restTemplate;

    /**
     * Маппер объектов.
     */
    protected final ObjectMapper objectMapper;

    /**
     * Маппер для объектов типа {@link Weather}.
     */
    protected final IWeatherMapper dtoMapper;

    /**
     * Конструктор.
     *
     * @param apiConfig    параметры конфигурации внешнего API
     * @param restTemplate клиент
     * @param objectMapper маппер объектов
     * @param dtoMapper    маппер для объектов типа {@link Weather}.
     */
    protected AbstractExternalWeatherApiService(
            final ExternalApiConfigParams apiConfig,
            final RestTemplate restTemplate,
            final ObjectMapper objectMapper,
            final IWeatherMapper dtoMapper
    ) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public ExternalApiWeatherResponse getCurrent(
            final String location,
            final String language,
            final Boolean aqi
    ) {
        final String url = buildUrl(location, language, aqi);
        final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        final HttpStatusCode statusCode = response.getStatusCode();
        final String responseBody = response.getBody();

        try {
            if (statusCode.is4xxClientError()) {
                handle4xxError(responseBody, location, url);
            }

            if (statusCode.is5xxServerError()) {
                throw new CommonServerSideException("Failed to get current weather from the external API");
            }

            return objectMapper.readValue(
                    responseBody,
                    ExternalApiWeatherResponse.class
            );

        } catch (JsonProcessingException e) {
            throw new ResponseBodyProceedingException(responseBody);
        }
    }

    /**
     * Создает URL для запроса.
     *
     * @param location  местоположение
     * @param language  язык
     * @param aqiNeeded надобность информации о качестве воздуха
     * @return URL для запроса
     */
    private String buildUrl(
            final String location,
            final String language,
            final Boolean aqiNeeded
    ) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        apiConfig.getBaseUrl() + apiConfig.getCurrent().getUrl()
                )
                .queryParam(apiConfig.getTokenParamName(), apiConfig.getToken())
                .queryParam(apiConfig.getCurrent().getLocationParamName(), location);

        if (language != null && !language.isEmpty()) {
            uriBuilder.queryParam(apiConfig.getCurrent().getLanguageParamName(), language);
        }

        if (Boolean.TRUE.equals(aqiNeeded)) {
            uriBuilder.queryParam(apiConfig.getCurrent().getAqiParamName(), "yes");
        }

        return uriBuilder.encode().toUriString();
    }

    /**
     * Обрабатывает ошибку с HTTP статусом 4xx.
     *
     * @param responseBody тело ответа
     * @param location     местоположение
     * @param url          URL запроса
     * @throws JsonProcessingException если не может преобразовать тело ответа
     *                                 в объект типа {@link ExternalApiErrorResponse}
     */
    private void handle4xxError(
            final String responseBody,
            final String location,
            final String url
    ) throws JsonProcessingException {

        final var errorResponse = objectMapper.readValue(
                responseBody,
                ExternalApiErrorResponse.class
        );

        final ErrorCode code = ErrorCode.withCode(errorResponse.getError().getCode());

        if (code == null) {
            throw new CommonServerSideException(
                    MessageFormat.format(
                            "Unknown API response error code: {0}",
                            errorResponse.getError().getCode()
                    )
            );
        }

        switch (code) {
            case LOCATION_NOT_PROVIDED -> throw new LocationNotProvidedException(LOCATION_PARAM_NAME);

            case LOCATION_NOT_FOUND -> throw new LocationNotFoundException(LOCATION_PARAM_NAME, location);

            case TOKEN_NOT_PROVIDED -> throw new TokenNotProvidedException(apiConfig.getTokenParamName());

            case TOKEN_LIMIT_EXCEEDED -> throw new TokenLimitExceededException(apiConfig.getTokenParamName());

            case DISABLED_TOKEN -> throw new DisabledTokenException(apiConfig.getTokenParamName());

            case INVALID_TOKEN -> throw new InvalidTokenException(apiConfig.getTokenParamName());

            case ACCESS_DENIED -> throw new AccessDeniedException(apiConfig.getTokenParamName());

            case INVALID_URL -> throw new InvalidUrlException(url);

            case INVALID_JSON -> throw new InvalidJsonBodyException();

            case TOO_MANY_LOCATIONS -> throw new TooManyLocationsException(
                    apiConfig.getBulkRequest().getLocationsLimit()
            );

            case EXTERNAL_API_ERROR -> throw new ExternalApiErrorException();
        }
    }
}
