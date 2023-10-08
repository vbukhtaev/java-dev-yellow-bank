package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * Ошибка в ответе внешнего API
 */
@Schema(description = "Ошибка в ответе внешнего API")
@Getter
@AllArgsConstructor
public class ApiError implements Serializable {

    /**
     * Код ошибки.
     */
    @Schema(description = "Код ошибки")
    @JsonProperty("code")
    private Integer code;

    /**
     * Сообщение.
     */
    @Schema(description = "Сообщение")
    @JsonProperty("message")
    private String message;
}
