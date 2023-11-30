package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.service.IWeatherProcessingService;
import ru.bukhtaev.service.crud.ICrudService;
import ru.bukhtaev.util.Accuracy;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.bukhtaev.controller.WeatherDataProcessingController.URL_API_WEATHER_PROCESSING;

/**
 * Контроллер обработки данных о погоде.
 */
@Tag(name = "Обработка данных")
@RestController
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasAuthority('weather-data:read')")
@RequestMapping(value = URL_API_WEATHER_PROCESSING, produces = "application/json")
public class WeatherDataProcessingController {

    /**
     * URL.
     */
    public static final String URL_API_WEATHER_PROCESSING = "/api/weather/processing";

    /**
     * Маппер для DTO данных о погоде.
     */
    private final IWeatherMapper mapper;

    /**
     * Сервис для обработки данных о погоде.
     */
    private final IWeatherProcessingService processingService;

    /**
     * Сервис CRUD операций над данными о погоде.
     */
    private final ICrudService<Weather, UUID> crudService;

    /**
     * Конструктор.
     *
     * @param mapper            маппер для DTO данных о погоде
     * @param processingService сервис для обработки данных о погоде
     * @param crudService       сервис CRUD операций над данными о погоде
     */
    @Autowired
    public WeatherDataProcessingController(
            final IWeatherMapper mapper,
            final IWeatherProcessingService processingService,
            @Qualifier("weatherCrudServiceJpa") final ICrudService<Weather, UUID> crudService) {
        this.processingService = processingService;
        this.crudService = crudService;
        this.mapper = mapper;
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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Double> getAverageTemperature(
            @Parameter(description = "Точность")
            @RequestParam(value = "precision", defaultValue = "2") final Integer precision
    ) {
        final List<Weather> data = crudService.getAll();

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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Map<String, Double>> getAverageTemperatures(
            @Parameter(description = "Точность")
            @RequestParam(value = "precision", defaultValue = "2") final Integer precision
    ) {
        final List<Weather> data = crudService.getAll();

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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Set<String>> getCitiesWarmer(
            @Parameter(description = "Температура")
            @RequestParam(value = "temperature") final Double temperature
    ) {
        final List<Weather> data = crudService.getAll();

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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Set<String>> getCitiesStrictlyWarmer(
            @Parameter(description = "Температура")
            @RequestParam(value = "temperature") final Double temperature
    ) {
        final List<Weather> data = crudService.getAll();

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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Map<UUID, List<Double>>> groupTemperaturesById() {
        final List<Weather> data = crudService.getAll();

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
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Map<Integer, List<WeatherResponseDto>>> groupByTemperature() {
        final List<Weather> data = crudService.getAll();

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

    @Operation(summary = "Получение температуры на текущее время по городу")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Температура найдена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Температура не найдена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("/current/{city}")
    @PreAuthorize("hasAuthority('weather-data:read')")
    public ResponseEntity<Double> get(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName,
            @Parameter(description = "Точность")
            @RequestParam(value = "accuracy", defaultValue = "DAYS") final Accuracy accuracy
    ) {
        return ResponseEntity.ok(
                processingService.getTemperature(cityName, ChronoUnit.valueOf(accuracy.name()))
        );
    }

    @Operation(summary = "Удаление всех данных о погоде для города")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Записи о погоде с указанным городом удалены"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @DeleteMapping("/for-city/{city}")
    @PreAuthorize("hasAuthority('weather-data:write')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName
    ) {
        processingService.delete(cityName);
    }
}
