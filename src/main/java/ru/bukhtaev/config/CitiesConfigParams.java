package ru.bukhtaev.config;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Параметры конфигурации для
 * получения данных о погоде по расписанию.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "weather")
public class CitiesConfigParams {

    /**
     * Названия городов.
     */
    @Size(min = 1)
    private String[] cities;
}
