package ru.bukhtaev.service;

import ru.bukhtaev.model.City;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.model.WeatherType;

import java.util.List;

/**
 * Сервис генерации данных о погоде.
 */
public interface IGenerationService {

    /**
     * Генерирует заданное количество записей о погоде в указанных городах с указанными типами погоды.
     *
     * @param cities города
     * @param types  типы погоды
     * @param count  количество записей
     * @return заданное количество записей о погоде в указанных городах
     */
    List<Weather> generate(
            final List<City> cities,
            final List<WeatherType> types,
            final int count
    );
}
