package ru.bukhtaev.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Информация о местоположении.
 */
@Schema(description = "Информация о местоположении")
@Getter
@Builder
public class LocationDto implements Serializable {

    /**
     * Название местоположения.
     */
    @Schema(description = "Название местоположения")
    @JsonProperty("name")
    private String name;

    /**
     * Регион или штат местоположения, если имеется.
     */
    @Schema(description = "Регион или штат местоположения, если имеется")
    @JsonProperty("region")
    private String region;

    /**
     * Страна расположения.
     */
    @Schema(description = "Страна расположения")
    @JsonProperty("country")
    private String country;

    /**
     * Широта в десятичных градусах.
     */
    @Schema(description = "Широта в десятичных градусах")
    @JsonProperty("lat")
    private BigDecimal lat;

    /**
     * Долгота в десятичных градусах.
     */
    @Schema(description = "Долгота в десятичных градусах")
    @JsonProperty("lon")
    private BigDecimal lon;

    /**
     * Название часового пояса.
     */
    @Schema(description = "Название часового пояса")
    @JsonProperty("tz_id")
    private String tzId;

    /**
     * Локальная дата и время в unix-времени.
     */
    @Schema(description = "Локальная дата и время в unix-времени")
    @JsonProperty("localtime_epoch")
    private Integer localtimeEpoch;

    /**
     * Местная дата и время.
     */
    @Schema(description = "Местная дата и время")
    @JsonProperty("localtime")
    private String localtime;
}
