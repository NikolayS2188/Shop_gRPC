package com.karkaror.shop.interceptor;

import com.karkaror.shop.annotation.Authorized;
import com.karkaror.shop.context.TokenContext;
import com.karkaror.shop.exception.ServiceUnavailableException;
import com.karkaror.shop.service.AuthClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * MIDDLEWARE приложения.
 * <p>
 * Это {@link HandlerInterceptor} Spring MVC: срабатывает до того,
 * как запрос попадёт в метод контроллера.
 * </p>
 * <p>
 * Логика:
 * <ol>
 *   <li>Если метод помечен {@link Authorized} → извлекаем JWT из HTTP-заголовка
 *       {@code Authorization: Bearer <token>}.</li>
 *   <li>Кладём токен в {@link TokenContext} (чтобы gRPC-клиент его увидел).</li>
 *   <li>Идём по gRPC в AuthService → ValidateToken.</li>
 *   <li>Если валиден → пропускаем (return true).</li>
 *   <li>Если нет → 401 и return false (в контроллер не попадём).</li>
 * </ol>
 * </p>
 * <p>
 * <b>Fail-closed:</b> если AuthService недоступен или не отвечает,
 * запрос <b>не пропускается</b> — возвращаем 503.
 * Это принцип безопасности: лучше отказать, чем пропустить без проверки.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthClientService authClientService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        // Пропускаем не-методы контроллера (например, статику)
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Проверяем наличие @Authorized
        if (!handlerMethod.hasMethodAnnotation(Authorized.class)) {
            return true;
        }

        // Извлекаем токен
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or malformed Authorization header for {}", request.getRequestURI());
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        TokenContext.set(token);

        // Проверяем через gRPC
        try {
            boolean valid = authClientService.validateToken(token);
            if (!valid) {
                log.debug("Token validation failed");
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return false;
            }
            return true;

        } catch (ServiceUnavailableException e) {
            // gRPC timeout / unavailable → 503 (не пропускаем)
            log.error("Auth service unavailable, rejecting request", e);
            sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication service unavailable. Please try again later.");
            return false;

        } catch (Exception e) {
            // Fail-closed: gRPC недоступен → 500, запрос не пропускаем
            log.error("Auth check failed unexpectedly", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication check failed");
            return false;
        }
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) {
        // Обязательно: чистим ThreadLocal, иначе утечка между запросами
        TokenContext.clear();
    }

    private void sendError(
            @NonNull HttpServletResponse response,
            int status,
            @NonNull String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
