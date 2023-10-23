package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.model.Weather;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Маппер для объектов типа {@link Weather}.
 */
@Mapper(
        componentModel = SPRING,
        uses = {
                ICityMapper.class,
                IWeatherTypeMapper.class
        }
)
public interface IWeatherMapper {

    /**
     * Конвертирует {@link Weather} в DTO {@link WeatherResponseDto}.
     *
     * @param entity {@link Weather}
     * @return DTO {@link WeatherResponseDto}
     */
    WeatherResponseDto convertToDto(final Weather entity);

    /**
     * Конвертирует DTO {@link WeatherRequestDto} в {@link Weather},
     * игнорируя поле {@code id}.
     *
     * @param dto DTO {@link WeatherRequestDto}
     * @return {@link Weather}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "cityId", target = "city.id")
    @Mapping(source = "typeId", target = "type.id")
    Weather convertFromDto(final WeatherRequestDto dto);
}
