package com.karkaror.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product create payload")
public class ProductRequestDTO {

    @NotBlank
    @Size(max = 200)
    @Schema(example = "Samsung Galaxy S24")
    private String name;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Smartphones")
    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Schema(example = "79999.99")
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Schema(example = "50")
    private Integer availableStock;

    @Schema(description = "Supplier id. Optional — product can exist without supplier.",
            example = "33333333-3333-3333-3333-333333333333",
            nullable = true)
    private UUID supplierId;
}
