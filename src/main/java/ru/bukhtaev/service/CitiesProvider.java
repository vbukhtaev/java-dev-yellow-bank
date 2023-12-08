package ru.bukhtaev.service;

import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.bukhtaev.config.CitiesConfigParams;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * Сервис предоставления названий городов.
 */
@Validated
@Component
public class CitiesProvider {

    /**
     * Двунаправленная очередь с названиями городов.
     */
    @Size(min = 1)
    private final Deque<String> deque;

    /**
     * Конструктор.
     *
     * @param citiesConfigParams параметры конфигурации для получения
     *                           данных о погоде по расписанию
     */
    public CitiesProvider(final CitiesConfigParams citiesConfigParams) {
        this.deque = Arrays.stream(citiesConfigParams.getCities())
                .distinct()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    /**
     * Возвращает города по очереди.
     *
     * @return города по очереди
     */
    public String getCity() {
        final String city = this.deque.poll();
        this.deque.addLast(city);
        return city;
    }
}
