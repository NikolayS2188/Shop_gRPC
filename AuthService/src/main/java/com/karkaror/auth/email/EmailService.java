package com.karkaror.auth.email;

/**
 * Заглушка отправки email.
 * <p>В реальном проекте — JavaMailSender/SendGrid/etc.
 *    В учебном — выводит письмо в консоль AuthService.</p>
 */
public interface EmailService {

    /** «Отправляет» письмо с новым паролем. */
    void sendPasswordResetEmail(String toEmail, String newPassword);
}
