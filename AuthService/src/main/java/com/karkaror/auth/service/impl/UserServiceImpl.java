package com.karkaror.auth.service.impl;

import com.karkaror.auth.email.EmailService;
import com.karkaror.auth.entity.User;
import com.karkaror.auth.exception.AuthenticationException;
import com.karkaror.auth.exception.ResourceNotFoundException;
import com.karkaror.auth.exception.UserAlreadyExistsException;
import com.karkaror.auth.password.PasswordService;
import com.karkaror.auth.repository.UserRepository;
import com.karkaror.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация {@link UserService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final EmailService emailService;

    @Override
    public User register(String email, String firstName, String lastName,
                         String phoneNumber, String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("User with email '" + email + "' already exists");
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .passwordHash(passwordService.hash(password))   // BCrypt-хэш
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!passwordService.matches(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Old password is incorrect");
        }

        user.setPasswordHash(passwordService.hash(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String email) {
        // С целью безопасности: всегда отвечаем success,
        // даже если email не найден (защита от перебора email).
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty()) {
            // Email не найден — не говорим об этом клиенту.
            // Логируем (для аудита), но возвращаем success.
            log.info("Password reset requested for unknown email: {}", email);
            return;                                                             // не бросаем исключение
        }

        User user = userOpt.get();

        // 1. Генерируем новый пароль
        String newPassword = passwordService.generateRandomPassword(12);

        // 2. Хэшируем и сохраняем
        user.setPasswordHash(passwordService.hash(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 3. «Отправляем» на email (заглушка — вывод в консоль)
        emailService.sendPasswordResetEmail(email, newPassword);
    }
}
