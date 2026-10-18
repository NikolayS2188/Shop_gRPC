package com.karkaror.shop.exception;

/**
 * Конфликт с текущим состоянием ресурса (например, дубликат).
 * <p>Соответствует HTTP 409 Conflict.</p>
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
