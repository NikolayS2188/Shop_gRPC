package com.karkaror.shop.controller;

import com.karkaror.shop.annotation.Authorized;
import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.ClientRequestDTO;
import com.karkaror.shop.dto.response.ClientResponseDTO;
import com.karkaror.shop.dto.response.ErrorResponse;
import com.karkaror.shop.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clients", description = "Client management endpoints")
public class ClientController {

    private final ClientService clientService;

    @Authorized
    @Operation(summary = "Create a new client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(dto));
    }

    @Authorized
    @Operation(summary = "Delete client by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Authorized
    @Operation(summary = "Find clients by name and surname")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ClientResponseDTO.class))))
    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> search(
            @RequestParam String name,
            @RequestParam String surname) {
        return ResponseEntity.ok(clientService.findByNameAndSurname(name, surname));
    }

    @Authorized
    @Operation(summary = "Get all clients with optional pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ClientResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> findAll(
            @Parameter(description = "Max number of items")
            @RequestParam(required = false) @Min(1) Integer limit,
            @Parameter(description = "Offset")
            @RequestParam(required = false) @Min(0) Integer offset) {
        return ResponseEntity.ok(clientService.findAll(limit, offset));
    }

    @Authorized
    @Operation(summary = "Update client address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ClientResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/address")
    public ResponseEntity<ClientResponseDTO> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(clientService.updateAddress(id, dto));
    }
}
