package ru.bukhtaev.external;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Параметры конфигурации внешнего API.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "external-api")
public class ExternalApiConfigParams {

    /**
     * Базовый URL.
     */
    @NotBlank
    private String baseUrl;

    /**
     * Токен для взаимодействия с внешним API.
     */
    @NotBlank
    private String token;

    /**
     * Название параметра для передачи токена внешнему API.
     */
    @NotBlank
    private String tokenParamName;

    /**
     * Параметры конфигурации эндпоинта внешнего API для погоды в реальном времени.
     */
    @Valid
    @NotNull
    @NestedConfigurationProperty
    private CurrentWeatherConfigParams current;

    /**
     * Параметры конфигурации эндпоинта внешнего API для массовых запросов.
     */
    @Valid
    @NotNull
    @NestedConfigurationProperty
    private BulkRequestConfigParams bulkRequest;
}
