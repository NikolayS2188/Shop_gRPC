package com.karkaror.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.karkaror.shop.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Client create/update payload")
public class ClientRequestDTO {

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Ivan")
    private String clientName;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Ivanov")
    private String clientSurname;

    @NotNull
    @Past
    @Schema(example = "1990-01-15")
    private LocalDate birthday;

    @NotNull
    @Schema(example = "MALE")
    private Gender gender;

    @Schema(description = "Existing address id. Mutually exclusive with 'address'.",
            example = "550e8400-e29b-41d4-a716-446655440000")
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
