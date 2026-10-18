package com.karkaror.shop.dto.response;

import com.karkaror.shop.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Client response")
public class ClientResponseDTO {

    @Schema(example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(example = "Ivan")
    private String clientName;

    @Schema(example = "Ivanov")
    private String clientSurname;

    @Schema(example = "1990-01-15")
    private LocalDate birthday;

    @Schema(example = "MALE")
    private Gender gender;

    @Schema(example = "2026-09-26T12:00:00")
    private LocalDateTime registrationDate;

    @Schema(description = "Client address")
    private AddressResponseDTO address;
}
