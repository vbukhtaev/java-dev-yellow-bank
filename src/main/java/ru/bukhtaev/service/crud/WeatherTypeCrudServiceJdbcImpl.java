package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jdbc.WeatherTypeJdbcRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.NameableEntity.FIELD_NAME;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME;

/**
 * JDBC-реализация сервиса CRUD операций над типами погоды.
 */
@Service("typeCrudServiceJdbc")
public class WeatherTypeCrudServiceJdbcImpl implements IWeatherTypeCrudService {

    /**
     * Репозиторий.
     */
    private final WeatherTypeJdbcRepository repository;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param repository      репозиторий
     * @param messageProvider сервис предоставления сообщений
     */
    @Autowired
    public WeatherTypeCrudServiceJdbcImpl(
            final WeatherTypeJdbcRepository repository,
            final MessageProvider messageProvider
    ) {
        this.repository = repository;
        this.messageProvider = messageProvider;
    }

    @Override
    public WeatherType getById(final UUID id) {
        return findById(id);
    }

    @Override
    public List<WeatherType> getAll() {
        return repository.findAll();
    }

    @Override
    public WeatherType create(@Valid final WeatherType newWeatherType) {
        repository.findFirstByName(newWeatherType.getName())
                .ifPresent(type -> {
                    throw new UniqueNameException(
                            messageProvider.getMessage(
                                    MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                                    type.getName()
                            ),
                            FIELD_NAME
                    );
                });
        return repository.create(newWeatherType);
    }

    @Override
    public void delete(final UUID id) {
        repository.deleteById(id);
    }

    @Override
    public WeatherType update(final UUID id, final WeatherType changedType) {
        repository.findFirstByNameWithAnotherId(changedType.getName(), id)
                .ifPresent(type -> {
                    throw new UniqueNameException(
                            messageProvider.getMessage(
                                    MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                                    type.getName()
                            ),
                            FIELD_NAME
                    );
                });

        final WeatherType typeToBeUpdated = findById(id);
        Optional.ofNullable(changedType.getName())
                .ifPresent(typeToBeUpdated::setName);
        return repository.change(id, typeToBeUpdated);
    }

    @Override
    public WeatherType replace(final UUID id, @Valid final WeatherType newType) {
        repository.findFirstByNameWithAnotherId(newType.getName(), id)
                .ifPresent(type -> {
                    throw new UniqueNameException(
                            messageProvider.getMessage(
                                    MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                                    type.getName()
                            ),
                            FIELD_NAME
                    );
                });

        final WeatherType existentWeatherType = findById(id);
        existentWeatherType.setName(newType.getName());
        return repository.change(id, existentWeatherType);
    }

    /**
     * Возвращает тип погоды с указанным ID, если он существует.
     * В противном случае выбрасывает {@link DataNotFoundException}.
     *
     * @param id ID
     * @return тип погоды с указанным ID, если он существует
     */
    private WeatherType findById(final UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND,
                                id
                        ),
                        FIELD_ID
                ));
    }
}
