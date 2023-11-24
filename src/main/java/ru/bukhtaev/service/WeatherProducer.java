package ru.bukhtaev.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;

import java.util.Locale;

/**
 * Сервис запроса данных о погоде из внешнего API
 * и их отправки в Kafka-топик по расписанию.
 */
@Slf4j
@Component
public class WeatherProducer {

    /**
     * Маппер для объектов типа {@link Weather}.
     */
    private final IWeatherMapper mapper;

    /**
     * Сервис предоставления названий городов.
     */
    private final CitiesProvider citiesProvider;

    /**
     * Сервис для отправки сообщений.
     */
    private final KafkaTemplate<String, Weather> kafkaTemplate;

    /**
     * Сервис для выполнения запросов к внешнему API данных о погоде.
     */
    private final IExternalWeatherApiService weatherApiService;

    /**
     * Конструктор.
     *
     * @param mapper            маппер для объектов типа {@link Weather}
     * @param citiesProvider    сервис предоставления названий городов
     * @param kafkaTemplate     сервис для отправки сообщений
     * @param weatherApiService сервис для выполнения запросов к внешнему API данных о погоде
     */
    @Autowired
    public WeatherProducer(
            final IWeatherMapper mapper,
            final CitiesProvider citiesProvider,
            final KafkaTemplate<String, Weather> kafkaTemplate,
            @Qualifier("weatherApiServiceJpa") final IExternalWeatherApiService weatherApiService
    ) {
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.citiesProvider = citiesProvider;
        this.weatherApiService = weatherApiService;
    }

    /**
     * Получает данные о погоде в данный момент времени
     * для указанного города и отправляет их в Kafka-топик.
     */
    @Scheduled(cron = "${weather.frequency}")
    private void publish() {

        final String location = citiesProvider.getCity();
        final String language = Locale.ENGLISH.getLanguage();

        final var weather = mapper.convertFromExternalDto(
                weatherApiService.getCurrent(
                        location,
                        language,
                        Boolean.FALSE
                )
        );

        sendWeather(weather);
    }

    /**
     * Отправляет данные о погоде в Kafka-топик по умолчанию.
     *
     * @param weather данные о погоде
     */
    private void sendWeather(final Weather weather) {
        final String cityName = weather.getCity().getName();
        kafkaTemplate.sendDefault(cityName, weather);
        log.info("Weather data for city <{}> was successfully sent", cityName);
    }
}
