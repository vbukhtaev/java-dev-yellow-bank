package ru.bukhtaev.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.bukhtaev.repository.jpa.IUserJpaRepository;
import ru.bukhtaev.validation.MessageProvider;

import static ru.bukhtaev.validation.MessageUtils.MESSAGE_CODE_USER_NOT_FOUND;

/**
 * Сервис предоставления данных о пользователе.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Сервис предоставления сообщений.
     */
    private final MessageProvider messageProvider;

    /**
     * Репозиторий пользователей.
     */
    private final IUserJpaRepository repository;

    /**
     * Конструктор.
     *
     * @param messageProvider сервис предоставления сообщений
     * @param repository      репозиторий пользователей
     */
    @Autowired
    public UserDetailsServiceImpl(
            final MessageProvider messageProvider,
            final IUserJpaRepository repository
    ) {
        this.messageProvider = messageProvider;
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
                .findByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageProvider.getMessage(
                                MESSAGE_CODE_USER_NOT_FOUND,
                                username
                        )
                ));
    }
}
