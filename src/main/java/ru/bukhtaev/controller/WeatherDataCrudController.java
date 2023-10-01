package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.service.IWeatherCrudService;
import ru.bukhtaev.util.Accuracy;

import java.time.temporal.ChronoUnit;

/**
 * Контроллер обработки CRUD операций над данными о погоде.
 */
@Tag(name = "CRUD операции")
@RestController
@RequestMapping("/api/weather")
public class WeatherDataCrudController {

    /**
     * Маппер для {@link WeatherRequestDto} и {@link WeatherResponseDto}.
     */
    private final IWeatherMapper mapper;

    /**
     * Сервис CRUD операций с данными о погоде.
     */
    private final IWeatherCrudService crudService;

    /**
     * Конструктор.
     *
     * @param mapper      маппер для {@link WeatherRequestDto} и {@link WeatherResponseDto}
     * @param crudService сервис CRUD операций с данными о погоде
     */
    @Autowired
    public WeatherDataCrudController(
            final IWeatherMapper mapper,
            final IWeatherCrudService crudService
    ) {
        this.mapper = mapper;
        this.crudService = crudService;
    }

    @Operation(summary = "Получение температуры на текущую дату по городу")
    @GetMapping("{city}")
    public ResponseEntity<Double> get(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName,
            @Parameter(description = "Точность")
            @RequestParam(value = "accuracy", defaultValue = "DAYS") final Accuracy accuracy
    ) {
        return ResponseEntity.ok(
                crudService.getTemperature(cityName, ChronoUnit.valueOf(accuracy.name()))
        );
    }

    @Operation(summary = "Добавление данных о погоде")
    @PostMapping("{city}")
    public ResponseEntity<WeatherResponseDto> create(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName,
            @RequestBody final WeatherRequestDto dto
    ) {
        final Weather newWeather = mapper.convertFromDto(dto);
        newWeather.setCityName(cityName);

        final Weather saved = crudService.create(newWeather);

        return new ResponseEntity<>(
                mapper.convertToDto(saved),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Обновление данных о погоде")
    @PutMapping("{city}")
    public ResponseEntity<WeatherResponseDto> update(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName,
            @RequestBody final WeatherRequestDto dto
    ) {
        final Weather weather = mapper.convertFromDto(dto);
        weather.setCityName(cityName);

        final Weather updated = crudService.update(weather);

        return new ResponseEntity<>(
                mapper.convertToDto(updated),
                HttpStatus.OK
        );
    }

    @Operation(summary = "Удаление всех данных о погоде для города")
    @DeleteMapping("{city}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName
    ) {
        crudService.remove(cityName);
    }
}
