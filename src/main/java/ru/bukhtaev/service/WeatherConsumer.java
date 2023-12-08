package ru.bukhtaev.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;

import static ru.bukhtaev.util.WeatherSort.DATE_TIME_DESC;

/**
 * Сервис получения и обработки данных о погоде из Kafka-топика.
 */
@Slf4j
@Component
public class WeatherConsumer {

    /**
     * Сервис для выполнения запросов к внешнему API данных о погоде.
     */
    private final IExternalWeatherApiService externalApiService;

    /**
     * Сервис для обработки данных о погоде.
     */
    private final IWeatherProcessingService processingService;

    /**
     * JPA-репозиторий данных о погоде.
     */
    private final IWeatherJpaRepository weatherRepository;

    /**
     * Конструктор.
     *
     * @param weatherRepository  JPA-репозиторий данных о погоде
     * @param processingService  сервис для обработки данных о погоде
     * @param externalApiService сервис для выполнения запросов к внешнему API данных о погоде
     */
    public WeatherConsumer(
            final IWeatherJpaRepository weatherRepository,
            final IWeatherProcessingService processingService,
            @Qualifier("weatherApiServiceJpa") final IExternalWeatherApiService externalApiService
    ) {
        this.externalApiService = externalApiService;
        this.processingService = processingService;
        this.weatherRepository = weatherRepository;
    }

    /**
     * Сохраняет полученные из Kafka-топика данные о погоде
     * и выводит в лог скользящее среднее за 30 периодов города,
     * который пришел в сообщении.
     *
     * @param weather полученные данные о погоде
     */
    @KafkaListener(topics = "${spring.kafka.template.default-topic}")
    private void process(final Weather weather) {
        final String cityName = weather.getCity().getName();
        log.info("Weather data for city <{}> was successfully received", cityName);

        final var optExistent = weatherRepository.findFirstByCityNameAndDateTime(
                cityName,
                weather.getDateTime()
        );

        if (optExistent.isEmpty()) {
            externalApiService.saveWithTransaction(weather);
            log.info("Weather data for city <{}> was successfully saved", cityName);
        }

        final var data = weatherRepository.findAllByCityName(
                cityName,
                PageRequest.of(
                        0,
                        30,
                        DATE_TIME_DESC.getSortValue()
                )
        );

        final var avgTemp = processingService.getAverageTemperature(data, 2);
        log.info(
                "Moving average temperature for city <{}> is: {}°C",
                cityName,
                avgTemp
        );
    }
}
