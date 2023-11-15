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
import ru.bukhtaev.dto.NameableRequestDto;
import ru.bukhtaev.dto.NameableResponseDto;
import ru.bukhtaev.dto.mapper.ICityMapper;
import ru.bukhtaev.model.City;
import ru.bukhtaev.service.crud.IDictionaryCrudService;
import ru.bukhtaev.validation.handling.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.bukhtaev.controller.CityRestController.URL_API_V1_CITIES;

/**
 * Контроллер обработки CRUD операций над городами.
 */
@Tag(name = "Города")
@RestController
@SecurityRequirement(name = "basicAuth")
@RequestMapping(value = URL_API_V1_CITIES, produces = "application/json")
public class CityRestController {

    /**
     * URL.
     */
    public static final String URL_API_V1_CITIES = "/api/v1/cities";

    /**
     * Сервис CRUD операций над городами.
     */
    private final IDictionaryCrudService<City, UUID> crudService;

    /**
     * Маппер для DTO городов.
     */
    private final ICityMapper mapper;

    /**
     * Конструктор.
     *
     * @param crudService сервис CRUD операций над городами
     * @param mapper      маппер для DTO городов
     */
    @Autowired
    public CityRestController(
            @Qualifier("cityCrudServiceJpa") final IDictionaryCrudService<City, UUID> crudService,
            final ICityMapper mapper
    ) {
        this.crudService = crudService;
        this.mapper = mapper;
    }

    @Operation(summary = "Получение всех городов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Города получены"
            )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('cities:read')")
    public ResponseEntity<List<NameableResponseDto>> handleGetAll() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        crudService.getAll()
                                .stream()
                                .map(mapper::convertToDto)
                                .toList()
                );
    }

    @Operation(summary = "Получение города по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Город получен"
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
                    description = "Город не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cities:read')")
    public ResponseEntity<NameableResponseDto> handleGetById(@PathVariable("id") final UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        mapper.convertToDto(crudService.getById(id))
                );
    }

    @Operation(summary = "Создание города")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Город создан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('cities:write')")
    public ResponseEntity<NameableResponseDto> handleCreate(
            @RequestBody final NameableRequestDto dto,
            final UriComponentsBuilder uriBuilder
    ) {
        final NameableResponseDto savedDto = mapper.convertToDto(
                crudService.create(
                        mapper.convertFromDto(dto)
                )
        );

        return ResponseEntity.created(uriBuilder
                        .path(URL_API_V1_CITIES + "/{id}")
                        .build(Map.of("id", savedDto.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(savedDto);
    }

    @Operation(summary = "Изменение города")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Город изменен"
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
                    description = "Город не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('cities:write')")
    public ResponseEntity<NameableResponseDto> handleUpdate(
            @PathVariable("id") final UUID id,
            @RequestBody final NameableRequestDto dto
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

    @Operation(summary = "Замена города")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Город заменен"
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
                    description = "Город не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cities:write')")
    public ResponseEntity<NameableResponseDto> handleReplace(
            @PathVariable("id") final UUID id,
            @RequestBody final NameableRequestDto dto
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

    @Operation(summary = "Удаление города по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Город удален"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cities:write')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleDelete(@PathVariable("id") final UUID id) {
        crudService.delete(id);
    }
}
