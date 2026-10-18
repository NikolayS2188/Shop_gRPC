package com.karkaror.shop.exception;

/**
 * Исключение для случая, когда внешний сервис недоступен
 * или не отвечает вовремя.
 * <p>
 * В отличие от {@link BadRequestException} (400) и
 * {@link ResourceNotFoundException} (404),
 * это временная проблема — клиент может повторить запрос позже.
 * </p>
 * <p>
 * Маппится в HTTP 503 Service Unavailable.
 * </p>
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
