package ru.bukhtaev.service;

import ru.bukhtaev.model.Weather;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис обработки данных о погоде.
 */
public interface WeatherService {

    /**
     * Вычисляет среднюю температуру для каждого региона в переданном наборе данных
     *
     * @return среднюю температуру для каждого региона в переданном наборе данных
     */
    Map<String, Double> averageTemperatures();

    /**
     * Вычисляет среднюю температуру в переданном наборе данных
     *
     * @return среднюю температуру в переданном наборе данных
     */
    double averageTemperature();

    /**
     * Определяет регионы, для которых хотя бы одно измерение со значением температуры выше указанной.
     *
     * @param temperature температура
     * @return регионы, для которых существуют измерения со значением температуры выше указанной
     */
    Set<String> getRegionsWarmerThan(final double temperature);

    /**
     * Определяет регионы, для которых все измерения со значением температуры выше указанной.
     *
     * @param temperature температура
     * @return регионы, для которых существуют измерения со значением температуры выше указанной
     */
    Set<String> getRegionsStrictlyWarmerThan(final double temperature);

    /**
     * Группирует значения температуры по идентификатору региона.
     *
     * @return сгруппированные по идентификатору региона значения температуры
     */
    Map<UUID, List<Double>> groupTemperaturesById();

    /**
     * Группирует измерения по температуре.
     *
     * @return сгруппированные по температуре измерения
     */
    Map<Integer, List<Weather>> groupByTemperature();
}
