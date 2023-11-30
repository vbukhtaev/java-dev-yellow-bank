package ru.bukhtaev.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.bukhtaev.dto.UserRequestDto;
import ru.bukhtaev.model.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Маппер для объектов типа {@link User}.
 */
@Mapper(componentModel = SPRING)
public interface IUserMapper {

    /**
     * Конвертирует DTO {@link UserRequestDto} в {@link User},
     * игнорируя поля {@code id} и {@code role}.
     *
     * @param dto DTO {@link UserRequestDto}
     * @return {@link User}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User convertFromDto(final UserRequestDto dto);
}
