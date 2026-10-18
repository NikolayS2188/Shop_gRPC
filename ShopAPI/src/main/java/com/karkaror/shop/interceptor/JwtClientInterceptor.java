package com.karkaror.shop.interceptor;

import com.karkaror.shop.context.TokenContext;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC ClientInterceptor: добавляет JWT в metadata каждого исходящего вызова.
 * <p>Metadata — аналог HTTP-заголовков в gRPC. Мы используем стандартный
 *    ключ {@code authorization: Bearer <token>}, чтобы его видели прокси
 *    (Istio/Envoy) и можно было применять политики.</p>
 * <p>Токен берётся из {@link TokenContext} (заполнен в {@code AuthInterceptor}).</p>
 */
@Component
@Slf4j
public class JwtClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String token = TokenContext.get();
                if (token != null) {
                    headers.put(AUTHORIZATION_KEY, "Bearer " + token);
                    log.debug("Attached JWT to gRPC call: {}", method.getFullMethodName());
                }
                super.start(responseListener, headers);
            }
        };
    }
}
