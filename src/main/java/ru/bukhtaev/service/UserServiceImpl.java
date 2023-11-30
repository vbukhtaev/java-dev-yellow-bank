package ru.bukhtaev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bukhtaev.exception.UniqueNameException;
import ru.bukhtaev.model.User;
import ru.bukhtaev.repository.jpa.IUserJpaRepository;
import ru.bukhtaev.security.Role;
import ru.bukhtaev.validation.MessageProvider;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static ru.bukhtaev.model.User.FIELD_USERNAME;
import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_USER_UNIQUE_USERNAME;

/**
 * Реализация сервиса для работы с пользователями.
 */
@Service
public class UserServiceImpl implements IUserService {

    /**
     * Репозиторий.
     */
    private final IUserJpaRepository repository;

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Сервис шифрования паролей.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор.
     *
     * @param repository      репозиторий
     * @param messageProvider сервис предоставления сообщений
     * @param passwordEncoder сервис шифрования паролей
     */
    @Autowired
    public UserServiceImpl(
            final IUserJpaRepository repository,
            final MessageProvider messageProvider,
            final PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.messageProvider = messageProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public User register(User newUser) {
        repository.findByUsername(newUser.getUsername())
                .ifPresent(user -> {
                    throw new UniqueNameException(
                            messageProvider.getMessage(
                                    MESSAGE_CODE_USER_UNIQUE_USERNAME,
                                    user.getUsername()
                            ),
                            FIELD_USERNAME
                    );
                });

        final String rawPassword = newUser.getPassword();
        final String password = passwordEncoder.encode(rawPassword);
        newUser.setPassword(password);
        newUser.setRole(Role.USER);
        return repository.save(newUser);
    }
}
