package com.karkaror.shop.exception;

/**
 * Клиент не аутентифицирован.
 * <p>Соответствует HTTP 401 Unauthorized.</p>
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
