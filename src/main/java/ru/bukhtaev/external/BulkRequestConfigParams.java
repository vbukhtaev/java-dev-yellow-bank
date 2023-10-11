package ru.bukhtaev.external;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Параметры конфигурации эндпоинта внешнего API для массовых запросов.
 */
@Getter
@Setter
public class BulkRequestConfigParams {

    /**
     * Лимит местоположений для массового запроса к внешнему API.
     */
    @Min(1)
    private Integer locationsLimit;
}
