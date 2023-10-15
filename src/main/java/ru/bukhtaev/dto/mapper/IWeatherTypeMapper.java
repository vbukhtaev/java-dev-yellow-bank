package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bukhtaev.dto.NameableRequestDto;
import ru.bukhtaev.dto.NameableResponseDto;
import ru.bukhtaev.model.WeatherType;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Маппер для объектов типа {@link WeatherType}.
 */
@Mapper(componentModel = SPRING)
public interface IWeatherTypeMapper {

    /**
     * Конвертирует {@link WeatherType} в DTO {@link NameableResponseDto}.
     *
     * @param entity {@link WeatherType}
     * @return DTO {@link NameableResponseDto}
     */
    NameableResponseDto convertToDto(final WeatherType entity);

    /**
     * Конвертирует DTO {@link NameableRequestDto} в {@link WeatherType},
     * игнорируя поле {@code id}.
     *
     * @param dto DTO {@link NameableRequestDto}
     * @return {@link WeatherType}
     */
    @Mapping(target = "id", ignore = true)
    WeatherType convertFromDto(final NameableRequestDto dto);
}
