package ru.bukhtaev.service;

import ru.bukhtaev.model.Weather;

import java.util.List;
import java.util.Set;

/**
 * Сервис генерации данных о погоде.
 */
public interface IGenerationService {

    /**
     * Генерирует заданное количество записей о погоде в указанных городах.
     *
     * @param cities названия городов
     * @param count  количество записей
     * @return заданное количество записей о погоде в указанных городах
     */
    List<Weather> generate(final Set<String> cities, final int count);
}
