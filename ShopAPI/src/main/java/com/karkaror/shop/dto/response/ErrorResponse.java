package com.karkaror.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Unified error response")
public class ErrorResponse {

    @Schema(example = "2026-09-26T14:30:00", description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(example = "400", description = "HTTP status code")
    private int status;

    @Schema(example = "Bad Request", description = "HTTP status reason phrase")
    private String error;

    @Schema(example = "clientName: must not be blank", description = "Detailed error message")
    private String message;

    @Schema(example = "/api/v1/clients", description = "Request path")
    private String path;
}
