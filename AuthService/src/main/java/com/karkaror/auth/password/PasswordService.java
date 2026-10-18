package com.karkaror.auth.password;

/**
 * Сервис хэширования паролей.
 * <p>Использует BCrypt — стойкий алгоритм с солью внутри хэша.
 *    MD5 и SHA1 не используются (устарели, уязвимы к brute-force).</p>
 */
public interface PasswordService {

    /** Хэширует пароль. Соль генерируется автоматически внутри BCrypt. */
    String hash(String rawPassword);

    /** Проверяет, соответствует ли открытый пароль сохранённому хэшу. */
    boolean matches(String rawPassword, String passwordHash);

    /** Генерирует случайный пароль заданной длины (для ResetPassword). */
    String generateRandomPassword(int length);
}
