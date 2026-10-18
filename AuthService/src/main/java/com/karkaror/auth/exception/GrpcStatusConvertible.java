package com.karkaror.auth.exception;

import io.grpc.Status;

/**
 * Интерфейс для исключений, которые умеют преобразовываться
 * в gRPC-статус.
 * <p>
 * Реализуя этот интерфейс, каждое исключение само знает,
 * в какой gRPC-статус его нужно преобразовать. Это устраняет
 * необходимость централизованного {@code switch}/{@code if}
 * в {@code GrpcExceptionHandler}.
 */
public interface GrpcStatusConvertible {

    /**
     * @return gRPC-статус, соответствующий этому исключению.
     */
    Status toGrpcStatus();
}
