package ru.bukhtaev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.bukhtaev.config.CitiesConfigParams;
import ru.bukhtaev.config.external.ExternalApiConfigParams;

@SpringBootApplication
@EnableConfigurationProperties({
        ExternalApiConfigParams.class,
        CitiesConfigParams.class
})
public class WeatherCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherCrudApplication.class, args);
    }
}
