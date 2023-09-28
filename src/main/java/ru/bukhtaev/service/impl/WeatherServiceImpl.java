package ru.bukhtaev.service.impl;

import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.service.WeatherService;
import ru.bukhtaev.util.DataNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

import static ru.bukhtaev.util.Utils.round;

/**
 * Реализация сервиса обработки данных о погоде.
 */
public class WeatherServiceImpl implements WeatherService {

    /**
     * Репозиторий данных о погоде.
     */
    private final Repository<Weather> repository;

    /**
     * Конструктор.
     *
     * @param repository репозиторий для данных о погоде
     */
    public WeatherServiceImpl(final Repository<Weather> repository) {
        this.repository = repository;
    }

    /**
     * Возвращает данные о погоде.
     *
     * @return данные о погоде
     * @throws DataNotFoundException если данных о погоде нет
     */
    private List<Weather> getAll() {
        final List<Weather> data = repository.findAll();

        if (data.isEmpty()) {
            throw new DataNotFoundException("There is no weather data!");
        }

        return data;
    }

    @Override
    public Map<String, Double> averageTemperatures() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Weather::getRegionName,
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Weather::getTemperature),
                                avgTemperature -> round(avgTemperature, 2)
                        )
                ));
    }

    @Override
    public double averageTemperature() {
        return round(getAll().stream()
                        .mapToDouble(Weather::getTemperature)
                        .average()
                        .orElseThrow(() -> new RuntimeException("Failed to compute average temperature!")),
                2
        );
    }

    @Override
    public Set<String> getRegionsWarmerThan(final double temperature) {
        return getAll().stream()
                .filter(weather -> weather.getTemperature() > temperature)
                .map(Weather::getRegionName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getRegionsStrictlyWarmerThan(final double temperature) {
        return getAll().stream()
                .collect(Collectors.groupingBy(Weather::getRegionName,
                        Collectors.mapping(weather -> weather.getTemperature() > temperature,
                                Collectors.toList())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(Boolean::booleanValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<UUID, List<Double>> groupTemperaturesById() {
        return getAll().stream()
                .collect(
                        Collectors.groupingBy(
                                Weather::getRegionId,
                                Collectors.mapping(
                                        Weather::getTemperature, Collectors.toList()
                                )
                        )
                );
    }

    @Override
    public Map<Integer, List<Weather>> groupByTemperature() {
        return new TreeMap<>(
                getAll().stream()
                        .collect(
                                Collectors.groupingBy(weather -> (int) Math.round(weather.getTemperature())
                                )
                        )
        );
    }
}
