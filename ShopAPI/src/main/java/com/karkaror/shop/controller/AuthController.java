package com.karkaror.shop.controller;

import com.karkaror.shop.dto.request.LoginRequestDTO;
import com.karkaror.shop.dto.request.RegisterRequestDTO;
import com.karkaror.shop.dto.request.ResetRequestDTO;
import com.karkaror.shop.dto.response.AuthResponseDTO;
import com.karkaror.shop.dto.response.ErrorResponse;
import com.karkaror.shop.dto.response.ResetResponseDTO;
import com.karkaror.shop.service.AuthClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Публичные эндпоинты аутентификации.
 * <p><b>Не защищены</b> {@code @Authorized} — доступны без токена.</p>
 * <p>Прокидывают запросы в gRPC AuthService.</p>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, password reset (public)")
public class AuthController {

    private final AuthClientService authClientService;

    @Operation(summary = "Register a new user",
            description = "Creates a user in AuthService and returns a JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authClientService.register(dto));
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/auth")
    public ResponseEntity<AuthResponseDTO> auth(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authClientService.login(dto));
    }

    @Operation(summary = "Reset password",
            description = "Generates a new password in AuthService and prints it to the console.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK (always success for security)",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset")
    public ResponseEntity<ResetResponseDTO> reset(@Valid @RequestBody ResetRequestDTO dto) {
        authClientService.resetPassword(dto.getEmail());
        return ResponseEntity.ok(ResetResponseDTO.builder().success(true).build());
    }
}
