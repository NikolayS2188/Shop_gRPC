package com.karkaror.shop.service;

import com.karkaror.shop.dto.request.LoginRequestDTO;
import com.karkaror.shop.dto.request.RegisterRequestDTO;
import com.karkaror.shop.dto.response.AuthResponseDTO;

/**
 * Клиент gRPC AuthService.
 * <p>ShopAPI ходит в AuthService по gRPC для аутентификации.
 *    JWT передаётся в metadata автоматически (см. JwtClientInterceptor).</p>
 */
public interface AuthClientService {

    AuthResponseDTO register(RegisterRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);

    boolean validateToken(String token);

    void resetPassword(String email);
}
