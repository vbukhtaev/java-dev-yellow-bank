package ru.bukhtaev.service;

import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;
import ru.bukhtaev.model.Weather;

/**
 * Сервис для выполнения запросов к внешнему API данных о погоде.
 */
public interface IExternalWeatherApiService {

    /**
     * Возвращает информацию о погоде в данный момент времени для указанного местоположения.
     *
     * @param location местоположение
     * @param language язык
     * @param aqi      надобность информации о качестве воздуха
     * @return информацию о погоде в данный момент времени для указанного местоположения
     */
    ExternalApiWeatherResponse getCurrent(
            final String location,
            final String language,
            final Boolean aqi
    );

    /**
     * Сохраняет данные о погоде, полученные от внешнего API, в транзакции.
     *
     * @param weather сохраненные данные о погоде
     */
    Weather saveWithTransaction(final Weather weather);
}
