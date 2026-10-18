package com.karkaror.auth.exception;

import io.grpc.Status;

/**
 * Ошибка аутентификации (неверный пароль, невалидный токен).
 * <p>Маппится в gRPC {@code UNAUTHENTICATED} → HTTP 401.</p>
 */
public class AuthenticationException extends RuntimeException implements GrpcStatusConvertible {

    public AuthenticationException(String message) {
        super(message);
    }

    @Override
    public Status toGrpcStatus() {
        return Status.UNAUTHENTICATED
                .withDescription(getMessage())
                .withCause(this);
    }
}
