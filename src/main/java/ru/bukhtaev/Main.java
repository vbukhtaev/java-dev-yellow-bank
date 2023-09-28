package ru.bukhtaev;

import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.Repository;
import ru.bukhtaev.repository.impl.InMemoryRepository;
import ru.bukhtaev.service.GenerationService;
import ru.bukhtaev.service.WeatherService;
import ru.bukhtaev.service.impl.GenerationServiceImpl;
import ru.bukhtaev.service.impl.WeatherServiceImpl;

public class Main {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {

        final Repository<Weather> repository = new InMemoryRepository(1024);

        final GenerationService generationService = new GenerationServiceImpl(repository);
        final WeatherService weatherService = new WeatherServiceImpl(repository);

        new Application(weatherService, generationService).run();
    }
}