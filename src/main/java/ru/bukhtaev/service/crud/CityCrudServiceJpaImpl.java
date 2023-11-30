package ru.bukhtaev.service.crud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.exception.DataNotFoundException;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.City;
import ru.bukhtaev.repository.jpa.ICityJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static ru.bukhtaev.model.BaseEntity.FIELD_ID;
import static ru.bukhtaev.model.NameableEntity.FIELD_NAME;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_NOT_FOUND;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_CITY_UNIQUE_NAME;

/**
 * JPA-реализация сервиса CRUD операций над городами.
 */
@Service("cityCrudServiceJpa")
@Transactional(
        isolation = READ_COMMITTED,
        readOnly = true
)
public class CityCrudServiceJpaImpl implements IDictionaryCrudService<City, UUID> {

    /**
     * Репозиторий.
     */
    private final ICityJpaRepository repository;

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
    public CityCrudServiceJpaImpl(
            final ICityJpaRepository repository,
            final MessageProvider messageProvider
    ) {
        this.repository = repository;
        this.messageProvider = messageProvider;
    }

    @Override
    public City getById(final UUID id) {
        return findById(id);
    }

    @Override
    public Optional<City> getByName(final String name) {
        return repository.findFirstByName(name);
    }

    @Override
    public List<City> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public City create(final City newCity) {
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
        return repository.save(newCity);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void delete(final UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public City update(final UUID id, final City changedCity) {
        repository.findFirstByNameAndIdNot(changedCity.getName(), id)
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
        return repository.save(cityToBeUpdated);
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public City replace(final UUID id, final City newCity) {
        repository.findFirstByNameAndIdNot(newCity.getName(), id)
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
        return repository.save(existentCity);
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
