package com.karkaror.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with JWT")
public class AuthResponseDTO {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...", description = "JWT access token")
    private String token;

    @Schema(example = "Bearer")
    private String type;

    @Schema(example = "11111111-1111-1111-1111-111111111111")
    private String userId;
}
