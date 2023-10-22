package ru.bukhtaev.service.crud;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.WeatherType;
import ru.bukhtaev.repository.jpa.IWeatherTypeJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_UNCOMMITTED;
import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.NameableEntity.FIELD_NAME;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME;

/**
 * JPA-реализация сервиса CRUD операций над типами погоды.
 */
@Service("typeCrudServiceJpa")
@Transactional(
        isolation = READ_UNCOMMITTED,
        readOnly = true
)
public class WeatherTypeCrudServiceJpaImpl implements IWeatherTypeCrudService {

    /**
     * Репозиторий.
     */
    private final IWeatherTypeJpaRepository repository;

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
    public WeatherTypeCrudServiceJpaImpl(
            final IWeatherTypeJpaRepository repository,
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
    public Optional<WeatherType> getByName(final String name) {
        return repository.findFirstByName(name);
    }

    @Override
    public List<WeatherType> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(isolation = READ_UNCOMMITTED)
    public WeatherType create(@Valid final WeatherType newType) {
        repository.findFirstByName(newType.getName())
                .ifPresent(type -> {
                    throw new UniqueNameException(
                            messageProvider.getMessage(
                                    MESSAGE_CODE_WEATHER_TYPE_UNIQUE_NAME,
                                    type.getName()
                            ),
                            FIELD_NAME
                    );
                });
        return repository.save(newType);
    }

    @Override
    @Transactional(isolation = READ_UNCOMMITTED)
    public void delete(final UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(isolation = READ_UNCOMMITTED)
    public WeatherType update(final UUID id, final WeatherType changedType) {
        repository.findFirstByNameAndIdNot(changedType.getName(), id)
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
        return repository.save(typeToBeUpdated);
    }

    @Override
    @Transactional(isolation = READ_UNCOMMITTED)
    public WeatherType replace(final UUID id, @Valid final WeatherType newType) {
        repository.findFirstByNameAndIdNot(newType.getName(), id)
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
        return repository.save(existentWeatherType);
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
