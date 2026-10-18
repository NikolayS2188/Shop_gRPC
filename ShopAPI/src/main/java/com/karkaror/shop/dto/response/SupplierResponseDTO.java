package com.karkaror.shop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Supplier response")
public class SupplierResponseDTO {

    @Schema(example = "33333333-3333-3333-3333-333333333333")
    private UUID id;

    @Schema(example = "TechSupplier Company")
    private String name;

    @Schema(example = "+7-999-123-45-67")
    private String phoneNumber;

    @Schema(description = "Supplier address")
    private AddressResponseDTO address;
}
