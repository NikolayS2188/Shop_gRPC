package com.karkaror.auth.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * gRPC ServerInterceptor: читает JWT из metadata и кладёт его в Context.
 * <p>Metadata — аналог HTTP-заголовков в gRPC (передаётся по HTTP/2).
 *    JWT приходит в ключе {@code authorization: Bearer <token>}.</p>
 * <p>Context — thread-local хранилище, доступное в методах gRPC-сервиса.</p>
 */
@Component
@Order(1)                   // запускается первым (потом остальные интерцепторы, если есть: GrpcExceptionHandler)
@Slf4j
public class AuthServerInterceptor implements ServerInterceptor {

    /** Ключ Context, по которому метод достанет токен. */
    public static final Context.Key<String> TOKEN_CONTEXT_KEY = Context.key("jwt-token");

    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String authHeader = headers.get(AUTHORIZATION_KEY);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring("Bearer ".length());
        }

        log.debug("gRPC call {} — token present: {}", call.getMethodDescriptor().getFullMethodName(), token != null);

        // Кладём токен в Context, чтобы метод мог его достать
        Context context = Context.current().withValue(TOKEN_CONTEXT_KEY, token);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
