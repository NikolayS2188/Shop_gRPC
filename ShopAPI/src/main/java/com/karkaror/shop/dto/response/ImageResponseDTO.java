package com.karkaror.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Image response")
public class ImageResponseDTO {

    @Schema(example = "55555555-5555-5555-5555-555555555555")
    private UUID imageId;
}
