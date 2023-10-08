package ru.bukhtaev.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;

/**
 * Сервис для выполнения запросов к внешнему API данных о погоде.
 */
@Validated
public interface IExternalWeatherApiService {

    /**
     * Возвращает информацию о погоде в данный момент времени для указанного местоположения.
     *
     * @param location местоположение
     * @param language язык
     * @param aqi      надобность информации о качестве воздуха
     * @return информацию о погоде в данный момент времени для указанного местоположения
     */
    ExternalApiWeatherResponse current(
            @NotBlank final String location,
            final String language,
            final Boolean aqi
    );
}
