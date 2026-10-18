package com.karkaror.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Supplier create payload")
public class SupplierRequestDTO {

    @NotBlank
    @Size(max = 150)
    @Schema(example = "TechSupplier Company")
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{5,20}$", message = "Invalid phone number")
    @Schema(example = "+7-999-123-45-67")
    private String phoneNumber;

    @Schema(description = "Existing address id. Mutually exclusive with 'address'.",
            example = "44444444-4444-4444-4444-444444444444")
    private UUID addressId;

    @Valid
    @Schema(description = "New address payload. Mutually exclusive with 'addressId'.")
    private AddressRequestDTO address;

    @JsonIgnore
    @AssertTrue(message = "Either addressId or address must be provided, but not both")
    @SuppressWarnings("unused")             // метод вызывается через reflection (Bean Validation)
    @Schema(hidden = true)
    public boolean isAddressProvided() {
        return (addressId != null) ^ (address != null);
    }
}
