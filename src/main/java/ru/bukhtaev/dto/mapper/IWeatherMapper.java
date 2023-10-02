package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.model.Weather;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Маппер для {@link WeatherRequestDto} и {@link WeatherResponseDto}.
 */
@Mapper(componentModel = SPRING)
public interface IWeatherMapper {

    /**
     * Конвертирует {@link Weather} в DTO {@link WeatherResponseDto}.
     *
     * @param entity {@link Weather}
     * @return DTO {@link WeatherResponseDto}
     */
    WeatherResponseDto convertToDto(Weather entity);

    /**
     * Конвертирует DTO {@link WeatherRequestDto} в {@link Weather},
     * игнорируя поля {@code cityId} и {@code cityName}.
     *
     * @param dto DTO {@link WeatherRequestDto}
     * @return {@link Weather}
     */
    @Mapping(target = "cityId", ignore = true)
    @Mapping(target = "cityName", ignore = true)
    Weather convertFromDto(WeatherRequestDto dto);
}
