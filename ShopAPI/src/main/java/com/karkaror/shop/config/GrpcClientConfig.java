package com.karkaror.shop.config;

import com.karkaror.shop.interceptor.DeadlineClientInterceptor;
import com.karkaror.shop.interceptor.JwtClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация gRPC-клиента для AuthService.
 * <p>
 * Создаёт {@link ManagedChannel} с двумя interceptor-ами:
 * <ol>
 *     <li>{@link DeadlineClientInterceptor} — добавляет deadline ко всем вызовам;</li>
 *     <li>{@link JwtClientInterceptor} — добавляет JWT в metadata.</li>
 * </ol>
 * </p>
 * <p>
 * <b>Порядок interceptor-ов:</b> не критичен для этих двух, так как они
 * модифицируют <b>разные</b> аспекты вызова (deadline vs headers).
 * Но в общем случае deadline лучше ставить <b>первым</b> — чтобы
 * другие interceptors работали в рамках известного времени.
 * </p>
 */
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.auth-service.address}")
    private String authServiceAddress;              // static://localhost:9090

    @Bean
    public ManagedChannel authServiceChannel(
            DeadlineClientInterceptor deadlineInterceptor,
            JwtClientInterceptor jwtInterceptor) {

        // static://localhost:9090 → localhost:9090
        String target = authServiceAddress.replace("static://", "");

        return ManagedChannelBuilder.forTarget(target)
                .usePlaintext()                                         // в проде, как минимум, нужно использовать — TLS.
                .intercept(deadlineInterceptor, jwtInterceptor)         // автоматически добавляет оба interceptor-а, Deadline и JWT.
                .build();
    }
}
