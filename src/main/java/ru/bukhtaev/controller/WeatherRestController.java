package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.service.crud.IWeatherCrudService;
import ru.bukhtaev.util.Accuracy;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер обработки CRUD операций над данными о погоде.
 */
@Tag(name = "Данные о погоде")
@RestController
@RequestMapping(value = "/api/v1/weather-data", produces = "application/json")
public class WeatherRestController {

    /**
     * Сервис CRUD операций над данными о погоде.
     */
    private final IWeatherCrudService crudService;

    /**
     * Маппер для DTO данных о погоде.
     */
    private final IWeatherMapper mapper;

    /**
     * Конструктор.
     *
     * @param crudService сервис CRUD операций над данными о погоде
     * @param mapper      маппер для DTO данных о погоде
     */
    @Autowired
    public WeatherRestController(
            @Qualifier("weatherCrudServiceJpa") final IWeatherCrudService crudService,
            final IWeatherMapper mapper
    ) {
        this.crudService = crudService;
        this.mapper = mapper;
    }

    @Operation(summary = "Получение всех данных о погоде")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные о погоде получены"
            )
    })
    @GetMapping
    public ResponseEntity<List<WeatherResponseDto>> handleGetAll() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        crudService.getAll()
                                .stream()
                                .map(mapper::convertToDto)
                                .toList()
                );
    }

    @Operation(summary = "Получение записи о погоде по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запись о погоде получена"
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
                    description = "Запись о погоде не найдена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<WeatherResponseDto> handleGetById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        mapper.convertToDto(crudService.getById(id))
                );
    }

    @Operation(summary = "Создание записи о погоде")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Запись о погоде создана"
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
                    description = "Город или тип погоды не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PostMapping
    public ResponseEntity<WeatherResponseDto> handleCreate(
            @RequestBody final WeatherRequestDto dto,
            final UriComponentsBuilder uriBuilder
    ) {
        final WeatherResponseDto savedDto = mapper.convertToDto(
                crudService.create(
                        mapper.convertFromDto(dto)
                )
        );

        return ResponseEntity.created(uriBuilder
                        .path("/api/v1/weather-data" + "/{id}")
                        .build(Map.of("id", savedDto.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(savedDto);
    }

    @Operation(summary = "Изменение записи о погоде")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запись о погоде изменена"
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
                    description = "Город или тип погоды не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<WeatherResponseDto> handleUpdate(
            @PathVariable("id") final UUID id,
            @RequestBody final WeatherRequestDto dto
    ) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        mapper.convertToDto(
                                crudService.update(
                                        id,
                                        mapper.convertFromDto(dto)
                                )
                        )
                );
    }

    @Operation(summary = "Замена записи о погоде")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запись о погоде заменена"
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
                    description = "Город или тип погоды не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<WeatherResponseDto> handleReplace(
            @PathVariable("id") final UUID id,
            @RequestBody final WeatherRequestDto dto
    ) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        mapper.convertToDto(
                                crudService.replace(
                                        id,
                                        mapper.convertFromDto(dto)
                                )
                        )
                );
    }

    @Operation(summary = "Удаление записи о погоде по ID")
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Запись о погоде удалена"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleDelete(@PathVariable("id") final UUID id) {
        crudService.delete(id);
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Название города")
            @PathVariable("city") final String cityName
    ) {
        crudService.delete(cityName);
    }
}
