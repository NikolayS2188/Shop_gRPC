package com.karkaror.auth.exception;

import io.grpc.Status;

/**
 * Пользователь с таким email уже существует.
 * <p>Маппится в gRPC {@code ALREADY_EXISTS} → HTTP 409.</p>
 */
public class UserAlreadyExistsException extends RuntimeException implements GrpcStatusConvertible {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public Status toGrpcStatus() {
        return Status.ALREADY_EXISTS
                .withDescription(getMessage())
                .withCause(this);
    }
}
