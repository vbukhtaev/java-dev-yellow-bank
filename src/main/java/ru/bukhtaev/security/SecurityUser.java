package ru.bukhtaev.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.bukhtaev.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Модель пользователя для интеграции со Spring Security.
 */
public class SecurityUser implements UserDetails {

    /**
     * Пользователь.
     */
    private final User user;

    /**
     * Конструктор.
     *
     * @param user пользователь
     */
    public SecurityUser(final User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.user.getRole()
                .getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(
                        permission.getDescriptor()
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
