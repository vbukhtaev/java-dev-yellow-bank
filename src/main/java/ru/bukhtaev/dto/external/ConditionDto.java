package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Погодные условия.
 */
@Schema(description = "Погодные условия")
@Getter
@Builder
public class ConditionDto implements Serializable {

    /**
     * Описание погодных условий.
     */
    @Schema(description = "Описание погодных условий")
    @JsonProperty("text")
    private String text;

    /**
     * URL иконки погодных условий.
     */
    @Schema(description = "URL иконки погодных условий")
    @JsonProperty("icon")
    private String icon;

    /**
     * Уникальный код погодных условий.
     */
    @Schema(description = "Уникальный код погодных условий")
    @JsonProperty("code")
    private Integer code;
}
