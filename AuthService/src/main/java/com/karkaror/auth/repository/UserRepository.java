package com.karkaror.auth.repository;

import com.karkaror.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Поиск пользователя по email (регистронезависимо). */
    Optional<User> findByEmailIgnoreCase(String email);

    /** Проверка существования email при регистрации. */
    boolean existsByEmailIgnoreCase(String email);
}
