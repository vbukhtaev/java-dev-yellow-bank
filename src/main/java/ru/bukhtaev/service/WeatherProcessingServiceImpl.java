package ru.bukhtaev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.bukhtaev.util.Utils.round;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_FAILED_TO_COMPUTE;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_THERE_IS_NO_DATA;

/**
 * Реализация сервиса обработки данных о погоде.
 */
@Service
public class WeatherProcessingServiceImpl implements IWeatherProcessingService {

    /**
     * Сервис предоставления сообщений/
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param messageProvider сервис предоставления сообщений
     */
    @Autowired
    public WeatherProcessingServiceImpl(final MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public Map<String, Double> getAverageTemperatures(final List<Weather> data, final int precision) {
        validate(data);

        return data.stream()
                .collect(Collectors.groupingBy(
                        Weather::getCityName,
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Weather::getTemperature),
                                avgTemperature -> round(avgTemperature, precision)
                        )
                ));
    }

    @Override
    public double getAverageTemperature(final List<Weather> data, final int precision) {
        validate(data);

        return round(data.stream()
                        .mapToDouble(Weather::getTemperature)
                        .average()
                        .orElseThrow(() -> new RuntimeException(
                                messageProvider.getMessage(MESSAGE_CODE_FAILED_TO_COMPUTE)
                        )),
                precision
        );
    }

    @Override
    public Set<String> getCitiesWarmerThan(final List<Weather> data, final double temperature) {
        validate(data);

        return data.stream()
                .filter(weather -> weather.getTemperature() > temperature)
                .map(Weather::getCityName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getCitiesStrictlyWarmerThan(final List<Weather> data, final double temperature) {
        validate(data);

        return data.stream()
                .collect(Collectors.groupingBy(Weather::getCityName,
                        Collectors.mapping(weather -> weather.getTemperature() > temperature,
                                Collectors.toList())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(Boolean::booleanValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<UUID, List<Double>> groupTemperaturesById(final List<Weather> data) {
        validate(data);

        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                Weather::getCityId,
                                Collectors.mapping(
                                        Weather::getTemperature, Collectors.toList()
                                )
                        )
                );
    }

    @Override
    public Map<Integer, List<Weather>> groupByTemperature(final List<Weather> data) {
        validate(data);

        return data.stream()
                .collect(
                        Collectors.groupingBy(
                                weather -> (int) Math.round(weather.getTemperature())
                        )
                );
    }

    /**
     * Проверяет наличие данных в переданном списке.
     *
     * @param data список с данными
     */
    private void validate(List<Weather> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage(MESSAGE_CODE_THERE_IS_NO_DATA)
            );
        }
    }
}
