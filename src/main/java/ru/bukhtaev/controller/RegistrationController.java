package ru.bukhtaev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bukhtaev.dto.UserRequestDto;
import ru.bukhtaev.dto.mapper.IUserMapper;
import ru.bukhtaev.model.User;
import ru.bukhtaev.service.IUserService;
import ru.bukhtaev.validation.MessageProvider;
import ru.bukhtaev.validation.handling.ErrorResponse;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static ru.bukhtaev.controller.RegistrationController.URL_SIGN_UP;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_USER_REGISTERED;

/**
 * Контроллер регистрации пользователей.
 */
@Tag(name = "Регистрация пользователей")
@RestController
@SecurityRequirement(name = "basicAuth")
@RequestMapping(URL_SIGN_UP)
public class RegistrationController {

    /**
     * URL.
     */
    public static final String URL_SIGN_UP = "/sign-up";

    /**
     * Сервис для работы с пользователями.
     */
    private final IUserService userService;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Маппер для DTO пользователей.
     */
    private final IUserMapper mapper;

    /**
     * Конструктор.
     *
     * @param userService     сервис для работы с пользователями
     * @param messageProvider сервис предоставления сообщений
     * @param mapper          маппер для DTO пользователей
     */
    @Autowired
    public RegistrationController(
            final IUserService userService,
            final MessageProvider messageProvider,
            final IUserMapper mapper
    ) {
        this.userService = userService;
        this.messageProvider = messageProvider;
        this.mapper = mapper;
    }

    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь зарегистрирован"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )}
            )
    })
    @PostMapping(produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleRegister(@RequestBody final UserRequestDto dto) {
        final User registered = userService.register(
                mapper.convertFromDto(dto)
        );

        return ResponseEntity.ok(
                messageProvider.getMessage(
                        MESSAGE_CODE_USER_REGISTERED,
                        registered.getUsername()
                )
        );
    }
}
