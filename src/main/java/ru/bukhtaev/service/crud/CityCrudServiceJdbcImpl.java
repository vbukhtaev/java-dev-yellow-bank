package ru.bukhtaev.service.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.repository.jdbc.CityJdbcRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED;
import static org.springframework.transaction.TransactionDefinition.ISOLATION_SERIALIZABLE;
import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.NameableEntity.FIELD_NAME;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_UNIQUE_NAME;

/**
 * JDBC-реализация сервиса CRUD операций над городами.
 */
@Service("cityCrudServiceJdbc")
public class CityCrudServiceJdbcImpl implements IDictionaryCrudService<City, UUID> {

    /**
     * Репозиторий.
     */
    private final CityJdbcRepository repository;

    /**
     * Объект для управления транзакциями.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Конструктор.
     *
     * @param repository          репозиторий
     * @param transactionTemplate объект для управления транзакциями
     * @param messageProvider     сервис предоставления сообщений
     */
    @Autowired
    public CityCrudServiceJdbcImpl(
            final CityJdbcRepository repository,
            final TransactionTemplate transactionTemplate,
            final MessageProvider messageProvider
    ) {
        this.repository = repository;
        this.transactionTemplate = transactionTemplate;
        this.messageProvider = messageProvider;
    }

    @Override
    public City getById(final UUID id) {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> findById(id));
    }

    @Override
    public Optional<City> getByName(final String name) {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> repository.findFirstByName(name));
    }

    @Override
    public List<City> getAll() {
        transactionTemplate.setReadOnly(true);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(status -> repository.findAll());
    }

    @Override
    public City create(final City newCity) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            repository.findFirstByName(newCity.getName())
                    .ifPresent(city -> {
                        throw new UniqueNameException(
                                messageProvider.getMessage(
                                        MESSAGE_CODE_CITY_UNIQUE_NAME,
                                        city.getName()
                                ),
                                FIELD_NAME
                        );
                    });
            return repository.create(newCity);
        });
    }

    @Override
    public void delete(final UUID id) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> repository.deleteById(id));
    }

    @Override
    public City update(final UUID id, final City changedCity) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            repository.findFirstByNameWithAnotherId(changedCity.getName(), id)
                    .ifPresent(city -> {
                        throw new UniqueNameException(
                                messageProvider.getMessage(
                                        MESSAGE_CODE_CITY_UNIQUE_NAME,
                                        city.getName()
                                ),
                                FIELD_NAME
                        );
                    });

            final City cityToBeUpdated = findById(id);
            Optional.ofNullable(changedCity.getName())
                    .ifPresent(cityToBeUpdated::setName);
            return repository.change(id, cityToBeUpdated);
        });
    }

    @Override
    public City replace(final UUID id, final City newCity) {
        transactionTemplate.setReadOnly(false);
        transactionTemplate.setIsolationLevel(ISOLATION_SERIALIZABLE);
        return transactionTemplate.execute(status -> {
            repository.findFirstByNameWithAnotherId(newCity.getName(), id)
                    .ifPresent(city -> {
                        throw new UniqueNameException(
                                messageProvider.getMessage(
                                        MESSAGE_CODE_CITY_UNIQUE_NAME,
                                        city.getName()
                                ),
                                FIELD_NAME
                        );
                    });

            final City existentCity = findById(id);
            existentCity.setName(newCity.getName());
            return repository.change(id, existentCity);
        });
    }

    /**
     * Возвращает город с указанным ID, если он существует.
     * В противном случае выбрасывает {@link DataNotFoundException}.
     *
     * @param id ID
     * @return город с указанным ID, если он существует
     */
    private City findById(final UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_CITY_NOT_FOUND,
                                id
                        ),
                        FIELD_ID
                ));
    }
}
