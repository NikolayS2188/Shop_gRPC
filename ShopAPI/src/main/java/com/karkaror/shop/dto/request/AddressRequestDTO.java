package com.karkaror.shop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Address payload")
public class AddressRequestDTO {

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Russia")
    private String country;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Moscow")
    private String city;

    @NotBlank
    @Size(max = 255)
    @Schema(example = "Tverskaya 1")
    private String street;
}
