package com.karkaror.shop.exception;

/**
 * Клиент аутентифицирован, но не имеет прав.
 * <p>Соответствует HTTP 403 Forbidden.</p>
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
