package ru.bukhtaev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.bukhtaev.model.City;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherJpaRepository;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.service.IGenerationService;

import java.util.List;

/**
 * Конфигурация наполнения начальными данными.
 */
@Configuration
public class GenerationConfig {

    /**
     * Количество требуемых записей о погоде.
     */
    @Value("${generation.count}")
    private Integer count;

    @Bean
    @Profile("!test")
    public ApplicationRunner dataLoader(
            final IGenerationService generator,
            final IWeatherJpaRepository repository,
            final ICityJpaRepository cityRepository,
            final IWeatherTypeJpaRepository typeRepository
    ) {
        return args -> {

            final List<City> cities = cityRepository.findAll();
            final List<WeatherType> types = typeRepository.findAll();

            if (!cities.isEmpty() && types.isEmpty()) {
                repository.saveAll(
                        generator.generate(
                                cities,
                                types,
                                count
                        )
                );
            }
        };
    }
}
