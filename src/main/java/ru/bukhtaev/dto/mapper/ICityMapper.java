package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bukhtaev.dto.NameableRequestDto;
import ru.bukhtaev.dto.NameableResponseDto;
import ru.bukhtaev.model.City;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Маппер для объектов типа {@link City}.
 */
@Mapper(componentModel = SPRING)
public interface ICityMapper {

    /**
     * Конвертирует {@link City} в DTO {@link NameableResponseDto}.
     *
     * @param entity {@link City}
     * @return DTO {@link NameableResponseDto}
     */
    NameableResponseDto convertToDto(final City entity);

    /**
     * Конвертирует DTO {@link NameableRequestDto} в {@link City},
     * игнорируя поле {@code id}.
     *
     * @param dto DTO {@link NameableRequestDto}
     * @return {@link City}
     */
    @Mapping(target = "id", ignore = true)
    City convertFromDto(final NameableRequestDto dto);
}
