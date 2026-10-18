package com.karkaror.auth.password.impl;

import com.karkaror.auth.password.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Реализация {@link PasswordService} на основе BCrypt.
 * <p>
 * {@link PasswordEncoder} инжектится через конструктор (DI).
 * Это позволяет:
 * <ul>
 *     <li>менять cost через конфиг;</li>
 *     <li>подменять encoder в тестах (mock).</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    private final PasswordEncoder passwordEncoder;                      // инжектится
    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }

    @Override
    public String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
