package com.karkaror.auth.service;

import com.karkaror.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Бизнес-логика пользователей.
 */
public interface UserService {

    User register(String email, String firstName, String lastName,
                  String phoneNumber, String password);

    Optional<User> findByEmail(String email);

    void changePassword(UUID userId, String oldPassword, String newPassword);

    void resetPassword(String email);
}
