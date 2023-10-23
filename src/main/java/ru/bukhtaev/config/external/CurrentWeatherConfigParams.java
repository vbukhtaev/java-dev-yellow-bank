package ru.bukhtaev.config.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Параметры конфигурации эндпоинта внешнего API для погоды в реальном времени.
 */
@Getter
@Setter
public class CurrentWeatherConfigParams {

    /**
     * URL данных о погоде в реальном времени.
     */
    @NotBlank
    private String url;

    /**
     * Название параметра для передачи местоположения внешнему API.
     */
    @NotBlank
    private String locationParamName;

    /**
     * Название параметра для передачи языка внешнему API.
     */
    @NotBlank
    private String languageParamName;

    /**
     * Название параметра для передачи надобности информации о качестве воздуха внешнему API.
     */
    @NotBlank
    private String aqiParamName;
}
