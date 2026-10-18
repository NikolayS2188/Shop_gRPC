package com.karkaror.auth.exception;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * gRPC-аналог {@code @RestControllerAdvice}.
 * <p>
 * gRPC не использует Spring MVC, поэтому {@code @ExceptionHandler}
 * не работает для gRPC-методов. Вместо этого — {@link ServerInterceptor},
 * который перехватывает исключения, выброшенные из методов gRPC-сервиса,
 * и превращает их в понятные gRPC-статусы.
 * </p>
 * <p>
 * <b>Как это работает:</b>
 * <ol>
 *     <li>Когда метод gRPC-сервиса выбрасывает исключение,
 *         gRPC-инфраструктура сама ловит его и вызывает
 *         {@code call.close(Status.UNKNOWN.withCause(t), ...)}.</li>
 *     <li>Наш interceptor оборачивает {@link ServerCall} и
 *         перехватывает {@link ServerCall#close}.</li>
 *     <li>Если статус — {@code UNKNOWN} и есть {@code cause},
 *         маппит его в конкретный статус ({@code INVALID_ARGUMENT},
 *         {@code ALREADY_EXISTS}, {@code NOT_FOUND} и т.д.).</li>
 * </ol>
 * </p>
 * <p>
 * <b>Почему не оборачиваем Listener:</b> gRPC-инфраструктура
 * <b>сама</b> ловит исключения из метода сервиса и вызывает
 * {@code call.close(...)}. Обёртка {@code Listener.onHalfClose()}
 * не получит исключение — оно уже поймано gRPC.
 * </p>
 * </p>
 * <p>
 * <b>Порядок interceptor-ов</b> ({@code @Order}):
 * <ul>
 *     <li>1 — {@code AuthServerInterceptor} (читает JWT из metadata);</li>
 *     <li>2 — {@code GrpcExceptionHandler} (ловит ошибки).</li>
 * </ul>
 * </p>
 * <p>
 * <b>Маппинг:</b> делегируется в {@link GrpcExceptionWrapper#toStatus}.
 * Собственные исключения реализуют {@link GrpcStatusConvertible} — они сами
 * знают свой статус. Чужие, библиотечные (например, {@code IllegalArgumentException})
 * обрабатываются в {@code GrpcExceptionWrapper}.
 * </p>
 */
@Component
@Order(2)                   // запускается после AuthServerInterceptor
@Slf4j
public class GrpcExceptionHandler implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Оборачиваем ServerCall для перехвата close():
        ServerCall<ReqT, RespT> wrappedCall =
                new ForwardingServerCall.SimpleForwardingServerCall<>(call) {

                    @Override
                    public void close(Status status, Metadata trailers) {
                        // gRPC при исключении в методе вызывает close(UNKNOWN.withCause(t))
                        if (status.getCode() == Status.Code.UNKNOWN && status.getCause() != null) {
                            Throwable cause = status.getCause();

                            log.warn("Exception in gRPC method {}: {}",
                                    call.getMethodDescriptor().getFullMethodName(),
                                    cause.getMessage(), cause);

                            // Маппинг в GrpcExceptionWrapper
                            status = GrpcExceptionWrapper.toStatus(cause);
                        }
                        super.close(status, trailers);
                    }
                };

        return next.startCall(wrappedCall, headers);
    }
}
