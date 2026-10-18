package com.karkaror.auth.email.impl;

import com.karkaror.auth.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link EmailService}, которая выводит «письмо» в консоль.
 * <p>Это заглушка по требованию задания: реальная отправка не нужна.</p>
 */
@Service
@Slf4j
public class ConsoleEmailServiceImpl implements EmailService {

    @Override
    public void sendPasswordResetEmail(String toEmail, String newPassword) {
        log.info("========================================");
        log.info(" EMAIL STUB — Password Reset");
        log.info(" To: {}", toEmail);
        log.info(" New password: {}", newPassword);
        log.info("========================================");
    }
}
