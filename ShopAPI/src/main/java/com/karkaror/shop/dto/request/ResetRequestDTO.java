package com.karkaror.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Password reset payload")
public class ResetRequestDTO {

    @NotBlank
    @Email
    @Schema(example = "user@example.com")
    private String email;
}
