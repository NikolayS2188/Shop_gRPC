package com.karkaror.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product response")
public class ProductResponseDTO {

    @Schema(example = "22222222-2222-2222-2222-222222222222")
    private UUID id;

    @Schema(example = "Samsung Galaxy S24")
    private String name;

    @Schema(example = "Smartphones")
    private String category;

    @Schema(example = "79999.99")
    private BigDecimal price;

    @Schema(example = "50")
    private Integer availableStock;

    @Schema(example = "2026-09-26T12:00:00")
    private LocalDateTime lastUpdateDate;

    @Schema(example = "33333333-3333-3333-3333-333333333333")
    private UUID supplierId;

    @Schema(example = "TechSupplier Company")
    private String supplierName;

    @Schema(example = "55555555-5555-5555-5555-555555555555", description = "Image id, null if no image")
    private UUID imageId;
}
