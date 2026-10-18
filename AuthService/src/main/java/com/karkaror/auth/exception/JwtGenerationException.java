package com.karkaror.auth.exception;

import io.grpc.Status;

/**
 * Ошибка генерации JWT.
 * <p>Маппится в gRPC {@code INTERNAL} → HTTP 500.</p>
 * <p>Описание статуса не содержит деталей — не раскрываем
 *    внутренние проблемы клиенту.</p>
 */
public class JwtGenerationException extends RuntimeException implements GrpcStatusConvertible {

    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Status toGrpcStatus() {
        return Status.INTERNAL
                .withDescription("Failed to generate token")
                .withCause(this);
    }
}
