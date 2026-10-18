package com.karkaror.auth.jwt;

import java.util.Optional;

/**
 * Сервис работы с JWT (HS256).
 * <p>Подписывает токены секретом и проверяет их валидность.</p>
 */
public interface JwtService {

    /** Создаёт JWT для пользователя. */
    String generateToken(String userId, String email);

    /** Проверяет подпись и срок действия. */
    boolean validate(String token);

    /** Извлекает userId из валидного токена. */
    Optional<String> extractUserId(String token);
}
