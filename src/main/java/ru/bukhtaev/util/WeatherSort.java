package ru.bukhtaev.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.NameableEntity.FIELD_NAME;
import static ru.bukhtaev.model.Weather.*;

/**
 * Варианты сортировки для данных о погоде.
 */
@Getter
@RequiredArgsConstructor
public enum WeatherSort {

    /**
     * По ID по возрастанию.
     */
    ID_ASC(Sort.by(Sort.Direction.ASC, FIELD_ID)),

    /**
     * По ID по убыванию.
     */
    ID_DESC(Sort.by(Sort.Direction.DESC, FIELD_ID)),

    /**
     * По температуре по возрастанию.
     */
    TEMPERATURE_ASC(Sort.by(Sort.Direction.ASC, FIELD_TEMPERATURE)),

    /**
     * По температуре по убыванию.
     */
    TEMPERATURE_DESC(Sort.by(Sort.Direction.DESC, FIELD_TEMPERATURE)),

    /**
     * По дате и времени по возрастанию.
     */
    DATE_TIME_ASC(Sort.by(Sort.Direction.ASC, FIELD_DATE_TIME)),

    /**
     * По дате и времени по убыванию.
     */
    DATE_TIME_DESC(Sort.by(Sort.Direction.DESC, FIELD_DATE_TIME)),

    /**
     * По названию города по возрастанию.
     */
    CITY_NAME_ASC(Sort.by(
            Sort.Direction.ASC,
            FIELD_CITY + "." + FIELD_NAME
    )),

    /**
     * По названию города по убыванию.
     */
    CITY_NAME_DESC(Sort.by(
            Sort.Direction.DESC,
            FIELD_CITY + "." + FIELD_NAME
    )),

    /**
     * По названию типа погоды по возрастанию.
     */
    TYPE_NAME_ASC(Sort.by(
            Sort.Direction.ASC,
            FIELD_TYPE + "." + FIELD_NAME
    )),

    /**
     * По названию типа погоды по убыванию.
     */
    TYPE_NAME_DESC(Sort.by(
            Sort.Direction.DESC,
            FIELD_TYPE + "." + FIELD_NAME
    ));

    private final Sort sortValue;
}
