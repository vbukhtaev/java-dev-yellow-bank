package ru.bukhtaev.service;

import java.util.Set;

/**
 * Сервис генерации данных о погоде.
 */
public interface GenerationService {

    /**
     * Генерирует заданное количество записей о погоде в указанных регионах.
     *
     * @param regions названия регионов
     * @param count   количество записей
     */
    void generate(final Set<String> regions, final int count);

    /**
     * Выводит все данные о погоде в консоль.
     */
    void print();

    /**
     * Удаляет все данные о погоде.
     */
    void clear();
}
