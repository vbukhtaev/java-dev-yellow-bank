package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.mapper.IWeatherMapper;
import ru.bukhtaev.model.Weather;
import ru.bukhtaev.service.crud.ICrudService;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.bukhtaev.controller.WeatherRestController.URL_API_V1_WEATHER_DATA;

/**
 * Контроллер обработки CRUD операций над данными о погоде.
 */
@Tag(name = "Данные о погоде")
@RestController
@SecurityRequirement(name = "basicAuth")
@RequestMapping(value = URL_API_V1_WEATHER_DATA, produces = "application/json")
public class WeatherRestController {

    /**
     * URL.
     */
    public static final String URL_API_V1_WEATHER_DATA = "/api/v1/weather-data";

    /**
     * Сервис CRUD операций над данными о погоде.
     */
    private final ICrudService<Weather, UUID> crudService;

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
            @Qualifier("weatherCrudServiceJpa") final ICrudService<Weather, UUID> crudService,
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
    @PreAuthorize("hasAuthority('weather-data:read')")
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
    @PreAuthorize("hasAuthority('weather-data:read')")
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
    @PreAuthorize("hasAuthority('weather-data:write')")
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
                        .path(URL_API_V1_WEATHER_DATA + "/{id}")
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
    @PreAuthorize("hasAuthority('weather-data:write')")
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
    @PreAuthorize("hasAuthority('weather-data:write')")
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
    @PreAuthorize("hasAuthority('weather-data:write')")
    public void handleDelete(@PathVariable("id") final UUID id) {
        crudService.delete(id);
    }
}
