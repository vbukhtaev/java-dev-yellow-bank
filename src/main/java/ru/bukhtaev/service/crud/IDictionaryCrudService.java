package ru.bukhtaev.service.crud;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Сервис CRUD операций над сущностями, имеющими название.
 * Поддерживает получение сущности по названию.
 *
 * @param <T>  тип сущности
 * @param <ID> тип ID
 */
@Validated
public interface IDictionaryCrudService<T, ID> extends ICrudService<T, ID> {

    /**
     * Возвращает объект типа {@link Optional} с сущностью типа {@link T}
     * с указанным названием, если она существует.
     * В противном случае возвращает пустой объект типа {@link Optional}.
     *
     * @param name название
     * @return объект типа {@link Optional} с сущностью типа {@link T}
     * с указанным названием, если она существует
     */
    Optional<T> getByName(@NotBlank final String name);
}
