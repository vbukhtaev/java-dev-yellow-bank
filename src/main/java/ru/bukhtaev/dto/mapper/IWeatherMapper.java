package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.bukhtaev.dto.WeatherRequestDto;
import ru.bukhtaev.dto.WeatherResponseDto;
import ru.bukhtaev.dto.external.ExternalApiWeatherResponse;
import ru.bukhtaev.exception.CommonServerSideException;
import ru.bukhtaev.model.Weather;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

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
     * Форматтер {@link LocalDateTime} для конвертации данных, полученных из внешнего API.
     */
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd H[H]:mm");

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

    /**
     * Конвертирует DTO {@link ExternalApiWeatherResponse} в {@link Weather},
     * игнорируя поле {@code id}.
     *
     * @param dto DTO {@link ExternalApiWeatherResponse}
     * @return {@link Weather}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city.id", ignore = true)
    @Mapping(target = "type.id", ignore = true)
    @Mapping(source = "location.name", target = "city.name")
    @Mapping(source = "current.condition.text", target = "type.name")
    @Mapping(source = "current.temperatureC", target = "temperature")
    @Mapping(source = "location.localtime", target = "dateTime", qualifiedByName = "toLocalDateTime")
    Weather convertFromExternalDto(final ExternalApiWeatherResponse dto);

    @Named("toLocalDateTime")
    static LocalDateTime convertStringToLocalDateTime(final String localTimeStr) {
        final TemporalAccessor temporalAccessor = FORMATTER.parseBest(
                localTimeStr,
                LocalDateTime::from,
                LocalDate::from
        );

        if (!(temporalAccessor instanceof LocalDateTime)) {
            throw new CommonServerSideException(
                    MessageFormat.format(
                            "Failed to map localtime string <{0}> to LocalDateTime instance!",
                            localTimeStr
                    )
            );
        }

        return (LocalDateTime) temporalAccessor;
    }
}
