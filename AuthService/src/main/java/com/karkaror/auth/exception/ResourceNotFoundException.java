package com.karkaror.auth.exception;

import io.grpc.Status;

/**
 * Ресурс не найден.
 * <p>Маппится в gRPC {@code NOT_FOUND} → HTTP 404.</p>
 */
public class ResourceNotFoundException extends RuntimeException implements GrpcStatusConvertible {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public Status toGrpcStatus() {
        return Status.NOT_FOUND
                .withDescription(getMessage())
                .withCause(this);
    }
}
