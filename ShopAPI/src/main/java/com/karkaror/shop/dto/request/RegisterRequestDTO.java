package com.karkaror.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO регистрации пользователя.
 * <p>Прокидывается в gRPC AuthService.Register.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration payload")
public class RegisterRequestDTO {

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Ivan")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Ivanov")
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{5,20}$", message = "Invalid phone number")
    @Schema(example = "+7-999-123-45-67")
    private String phoneNumber;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "password123", description = "Password (min 8 chars)")
    private String password;
}
