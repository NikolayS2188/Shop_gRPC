package com.karkaror.auth.grpc;

import com.karkaror.auth.entity.User;
import com.karkaror.auth.exception.AuthenticationException;
import com.karkaror.auth.grpc.AuthServiceProto.*;
import com.karkaror.auth.jwt.JwtService;
import com.karkaror.auth.password.PasswordService;
import com.karkaror.auth.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * Реализация gRPC-сервиса AuthService.
 * <p>
 * <b>Без try-catch:</b> исключения летят наверх, их ловит
 * {@code GrpcExceptionHandler} (ServerInterceptor) и маппит
 * в понятные gRPC-статусы.
 * </p>
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> observer) {
        // Исключения ловит интерцептор GrpcExceptionHandler.

        validateRegisterRequest(request);

        User user = userService.register(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getId().toString(), user.getEmail());

        observer.onNext(AuthResponse.newBuilder()
                .setToken(token)
                .setUserId(user.getId().toString())
                .build());
        observer.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> observer) {
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId().toString(), user.getEmail());

        observer.onNext(AuthResponse.newBuilder()
                .setToken(token)
                .setUserId(user.getId().toString())
                .build());
        observer.onCompleted();
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> observer) {
        String token = AuthServerInterceptor.TOKEN_CONTEXT_KEY.get();

        if (token == null || !jwtService.validate(token)) {
            observer.onNext(ValidateTokenResponse.newBuilder().setValid(false).build());
            observer.onCompleted();
            return;
        }

        String userId = jwtService.extractUserId(token).orElse("");
        observer.onNext(ValidateTokenResponse.newBuilder()
                .setValid(true)
                .setUserId(userId)
                .build());
        observer.onCompleted();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> observer) {
        if (request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }

        UUID userId;
        try {
            userId = UUID.fromString(request.getUserId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId format");
        }
        userService.changePassword(
                userId,
                request.getOldPassword(),
                request.getNewPassword()
        );
        observer.onNext(ChangePasswordResponse.newBuilder().setSuccess(true).build());
        observer.onCompleted();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> observer) {
        if (request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        userService.resetPassword(request.getEmail());
        observer.onNext(ResetPasswordResponse.newBuilder().setSuccess(true).build());
        observer.onCompleted();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("firstName is required");
        }
        if (request.getLastName().isBlank()) {
            throw new IllegalArgumentException("lastName is required");
        }
        if (request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("phoneNumber is required");
        }
        if (request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("password must be at least 8 characters");
        }
    }
}
