package com.karkaror.shop.service.impl;

import com.karkaror.auth.grpc.AuthResponse;
import com.karkaror.auth.grpc.AuthServiceGrpc;
import com.karkaror.auth.grpc.LoginRequest;
import com.karkaror.auth.grpc.RegisterRequest;
import com.karkaror.auth.grpc.ResetPasswordRequest;
import com.karkaror.auth.grpc.ResetPasswordResponse;
import com.karkaror.auth.grpc.ValidateTokenRequest;
import com.karkaror.auth.grpc.ValidateTokenResponse;
import com.karkaror.shop.dto.request.LoginRequestDTO;
import com.karkaror.shop.dto.request.RegisterRequestDTO;
import com.karkaror.shop.dto.response.AuthResponseDTO;
import com.karkaror.shop.exception.BadRequestException;
import com.karkaror.shop.exception.ConflictException;
import com.karkaror.shop.exception.ForbiddenException;
import com.karkaror.shop.exception.ResourceNotFoundException;
import com.karkaror.shop.exception.ServiceUnavailableException;
import com.karkaror.shop.exception.UnauthorizedException;
import com.karkaror.shop.service.AuthClientService;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link AuthClientService} через gRPC-stub.
 * <p>
 * Deadline устанавливается автоматически через
 * {@code DeadlineClientInterceptor} (в {@code GrpcClientConfig}).
 * </p>
 */
@Service
@Slf4j
public class AuthClientServiceImpl implements AuthClientService {

    private final ManagedChannel channel;
    private final AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    public AuthClientServiceImpl(ManagedChannel authServiceChannel) {
        this.channel = authServiceChannel;
        this.blockingStub = AuthServiceGrpc.newBlockingStub(authServiceChannel);
        // deadline уже добавлен через DeadlineClientInterceptor в канале
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        try {
            AuthResponse response = blockingStub.register(RegisterRequest.newBuilder()
                    .setEmail(dto.getEmail())
                    .setFirstName(dto.getFirstName())
                    .setLastName(dto.getLastName())
                    .setPhoneNumber(dto.getPhoneNumber())
                    .setPassword(dto.getPassword())
                    .build());

            return AuthResponseDTO.builder()
                    .token(response.getToken())
                    .type("Bearer")
                    .userId(response.getUserId())
                    .build();

        } catch (StatusRuntimeException e) {
            handleGrpcError(e, "register");
            return null;                            // недостижимо: handleGrpcError всегда бросает
        }
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            AuthResponse response = blockingStub.login(LoginRequest.newBuilder()
                    .setEmail(dto.getEmail())
                    .setPassword(dto.getPassword())
                    .build());

            return AuthResponseDTO.builder()
                    .token(response.getToken())
                    .type("Bearer")
                    .userId(response.getUserId())
                    .build();

        } catch (StatusRuntimeException e) {
            handleGrpcError(e, "login");
            return null;
        }
    }

    @Override
    public boolean validateToken(String token) {
        // Токен передаётся автоматически через metadata (JwtClientInterceptor)
        try {
            ValidateTokenResponse response = blockingStub.validateToken(
                    ValidateTokenRequest.newBuilder().build());
            return response.getValid();
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            log.debug("Token validation failed: {} — {}", code, e.getStatus().getDescription());

            // 503 Service Unavailable — AuthService недоступен, fail-closed
            if (code == Status.Code.DEADLINE_EXCEEDED || code == Status.Code.UNAVAILABLE) {
                log.error("AuthService unavailable in validateToken: {}", code);
                throw new ServiceUnavailableException(
                        "Authentication service unavailable. Please try again later.");
            }

            // Остальные ошибки — токен невалиден (fail-closed) → 401
            return false;                                         // недостижимо
        }
    }

    @Override
    public void resetPassword(String email) {
        try {
            ResetPasswordResponse response = blockingStub.resetPassword(ResetPasswordRequest.newBuilder()
                    .setEmail(email)
                    .build());

            // Defensive check - на случай изменения сервера (сервер должен вернуть success=true, если не бросил исключение):
            if (!response.getSuccess()) {
                log.warn("Password reset returned success=false for {}", email);
                throw new ServiceUnavailableException("Password reset failed. Please try again later.");
            }

            log.info("Password reset email sent to {}", email);

        } catch (StatusRuntimeException e) {
            handleGrpcError(e, "resetPassword");
        }
    }

    /**
     * Маппинг gRPC-статусов в HTTP-исключения.
     * <p>
     * Особое внимание — {@link Status.Code#DEADLINE_EXCEEDED} и
     * {@link Status.Code#UNAVAILABLE}: это не ошибки приложения,
     * а проблемы с сетью/сервисом. Возвращаем 503 Service Unavailable,
     * а не 500, чтобы клиент знал, что это временная проблема.
     * </p>
     */
    private void handleGrpcError(StatusRuntimeException e, String operation) {
        Status.Code code = e.getStatus().getCode();
        String desc = e.getStatus().getDescription();
        log.warn("gRPC {} failed: {} — {}", operation, code, desc);

        switch (code) {
            // 409 Conflict — ресурс уже существует
            case ALREADY_EXISTS ->
                    throw new ConflictException(desc);

            // 401 Unauthorized — не аутентифицирован
            case UNAUTHENTICATED ->
                    throw new UnauthorizedException(desc);

            // 403 Forbidden — аутентифицирован, но нет прав
            case PERMISSION_DENIED ->
                    throw new ForbiddenException(desc);

            // 400 Bad Request — невалидные данные
            case INVALID_ARGUMENT ->
                    throw new BadRequestException(desc);

            // 404 Not Found
            case NOT_FOUND ->
                    throw new ResourceNotFoundException(desc);

            // 503 Service Unavailable — таймаут
            case DEADLINE_EXCEEDED -> {
                log.error("AuthService did not respond within deadline (operation: {})", operation);
                throw new ServiceUnavailableException(
                        "Authentication service timeout. Please try again later.");
            }

            // 503 Service Unavailable — сервис недоступен
            case UNAVAILABLE -> {
                log.error("AuthService unavailable (operation: {})", operation);
                throw new ServiceUnavailableException(
                        "Authentication service is unavailable. Please try again later.");
            }

            // 500 Internal Server Error — неожиданное
            default -> {
                log.error("Unexpected gRPC error in {}: {}", operation, code, e);
                throw new RuntimeException("Auth service error: " + desc, e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdown();
    }
}
