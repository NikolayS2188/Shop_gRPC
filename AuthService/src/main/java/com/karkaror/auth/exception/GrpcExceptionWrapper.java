package com.karkaror.auth.exception;

import io.grpc.Status;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Обёртка для «чужих» исключений, которые нужно маппить в gRPC-статус.
 * <p>
 * Используется, когда исходное исключение нельзя изменить
 * (например, {@code IllegalArgumentException} из JDK).
 * </p>
 */
public final class GrpcExceptionWrapper {

    private GrpcExceptionWrapper() {}

    /**
     * Пытается преобразовать любое {@link Throwable} в {@link Status}.
     * <p>
     * Порядок:
     * <ol>
     *     <li>Если реализует {@link GrpcStatusConvertible} — используем его.</li>
     *     <li>Если {@code IllegalArgumentException} — {@code INVALID_ARGUMENT}.</li>
     *     <li>Если {@code IllegalStateException} — {@code FAILED_PRECONDITION}.</li>
     *     <li>Если {@code SecurityException} — {@code PERMISSION_DENIED}.</li>
     *     <li>Если {@code DataIntegrityViolationException} — {@code ALREADY_EXISTS}.</li>
     *     <li>Иначе — {@code INTERNAL}.</li>
     * </ol>
     * </p>
     */
    public static Status toStatus(Throwable cause) {
        // 1. Наши исключения
        if (cause instanceof GrpcStatusConvertible convertible) {
            return convertible.toGrpcStatus();
        }

        // 2. Чужие исключения
        if (cause instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(cause.getMessage())
                    .withCause(cause);
        }
        if (cause instanceof IllegalStateException) {
            return Status.FAILED_PRECONDITION
                    .withDescription(cause.getMessage())
                    .withCause(cause);
        }
        if (cause instanceof SecurityException) {
            return Status.PERMISSION_DENIED
                    .withDescription(cause.getMessage())
                    .withCause(cause);
        }
        if (cause instanceof DataIntegrityViolationException) {
            return Status.ALREADY_EXISTS
                    .withDescription(cause.getMessage())
                    .withCause(cause);
        }

        // 3. Fallback
        return Status.INTERNAL
                .withDescription("Internal server error")
                .withCause(cause);
    }
}
