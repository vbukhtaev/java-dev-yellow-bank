package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.repository.IRepository;
import ru.bukhtaev.service.IWeatherProcessingService;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Контроллер обработки данных о погоде.
 */
@Tag(name = "Обработка данных")
@RestController
@RequestMapping(value = "/api/weather/processing", produces = "application/json")
public class WeatherDataProcessingController {

    /**
     * Маппер для {@link WeatherRequestDto} и {@link WeatherResponseDto}.
     */
    private final IWeatherMapper mapper;

    /**
     * Сервис для работы с данными о погоде.
     */
    private final IWeatherProcessingService processingService;

    /**
     * Репозиторий данных о погоде.
     */
    private final IRepository<Weather> repository;

    /**
     * Конструктор.
     *
     * @param mapper            маппер для {@link WeatherRequestDto} и {@link WeatherResponseDto}
     * @param processingService сервис для работы с данными о погоде
     * @param repository        репозиторий данных о погоде
     */
    @Autowired
    public WeatherDataProcessingController(
            final IWeatherMapper mapper,
            final IWeatherProcessingService processingService,
            final IRepository<Weather> repository) {
        this.mapper = mapper;
        this.processingService = processingService;
        this.repository = repository;
    }

    @Operation(summary = "Получение общей средней температуры")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Общая средняя температура получена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("average-temperature")
    public ResponseEntity<Double> getAverageTemperature(
            @Parameter(description = "Точность")
            @RequestParam(value = "precision", defaultValue = "2") final Integer precision
    ) {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.getAverageTemperature(data, precision)
        );
    }

    @Operation(summary = "Получение средней температуры для каждого города")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Средняя температура для каждого города получена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("average-temperatures")
    public ResponseEntity<Map<String, Double>> getAverageTemperatures(
            @Parameter(description = "Точность")
            @RequestParam(value = "precision", defaultValue = "2") final Integer precision
    ) {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.getAverageTemperatures(data, precision)
        );
    }

    @Operation(
            summary = "Получение городов с температурой выше указанной",
            description = "Получение городов с хотя бы одним измерением температуры выше указанной"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Города с температурой выше указанной получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("cities-warmer")
    public ResponseEntity<Set<String>> getCitiesWarmer(
            @Parameter(description = "Температура")
            @RequestParam(value = "temperature") final Double temperature
    ) {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.getCitiesWarmerThan(data, temperature)
        );
    }

    @Operation(
            summary = "Получение городов с температурой строго выше указанной",
            description = "Получение городов со всеми измерениями температуры выше указанной"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Города с температурой строго выше указанной получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("cities-strictly-warmer")
    public ResponseEntity<Set<String>> getCitiesStrictlyWarmer(
            @Parameter(description = "Температура")
            @RequestParam(value = "temperature") final Double temperature
    ) {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.getCitiesStrictlyWarmerThan(data, temperature)
        );
    }

    @Operation(summary = "Группировка температур по ID городов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сгруппированные по ID температуры получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("grouped-by-id")
    public ResponseEntity<Map<UUID, List<Double>>> groupTemperaturesById() {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.groupTemperaturesById(data)
        );
    }

    @Operation(summary = "Группировка данных по температуре")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сгруппированные по температуре данные получены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Обрабатываемые данные некорректны",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("grouped-by-temperature")
    public ResponseEntity<Map<Integer, List<WeatherResponseDto>>> groupByTemperature() {
        final List<Weather> data = repository.findAll();

        return ResponseEntity.ok(
                processingService.groupByTemperature(data)
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue()
                                        .stream()
                                        .map(mapper::convertToDto)
                                        .toList())
                        )
        );
    }
}
