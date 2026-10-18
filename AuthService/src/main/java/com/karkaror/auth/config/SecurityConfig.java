package com.karkaror.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Конфигурация безопасности AuthService.
 * <p>
 * Здесь мы не используем {@code spring-boot-starter-security} —
 * только модуль {@code spring-security-crypto} для BCrypt.
 * Это значит, что:
 * <ul>
 *     <li>нет авто-конфигурации Spring Security (фильтров, цепочек);</li>
 *     <li>нет {@code SecurityFilterChain} — аутентификация идёт через gRPC;</li>
 *     <li>единственное, что нам нужно от Spring Security — {@link PasswordEncoder}.</li>
 * </ul>
 * </p>
 * <p>
 * {@link BCryptPasswordEncoder} — адаптивная хэш-функция:
 * стоимость (cost) можно увеличивать со временем, не ломая старые хэши.
 * Соль генерируется автоматически и хранится внутри хэша,
 * поэтому отдельного поля в БД не требуется.
 * </p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Cost для BCrypt. Значение 12 = 2^12 = 4096 итераций.
     * <p>
     * Почему 12:
     * <ul>
     *     <li>10 — быстро, но слабее;</li>
     *     <li>12 — баланс: ~250 мс на хэш (приемлемо для логина);</li>
     *     <li>14+ — очень медленно, может влиять на UX.</li>
     * </ul>
     * Настраивается через {@code application.properties} — можно менять
     * без пересборки.
     * </p>
     */
    @Value("${bcrypt.cost:12}")
    private int bcryptCost;

    /**
     * Бин {@link PasswordEncoder} с алгоритмом BCrypt.
     * <p>
     * Инжектится в {@code PasswordServiceImpl} через конструктор.
     * В тестах легко подменить на mock.
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptCost);
    }
}
