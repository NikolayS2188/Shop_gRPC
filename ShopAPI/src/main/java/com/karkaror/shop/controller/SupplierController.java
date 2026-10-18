package com.karkaror.shop.controller;

import com.karkaror.shop.annotation.Authorized;
import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.SupplierRequestDTO;
import com.karkaror.shop.dto.response.ErrorResponse;
import com.karkaror.shop.dto.response.SupplierResponseDTO;
import com.karkaror.shop.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier management endpoints")
public class SupplierController {

    private final SupplierService supplierService;

    @Authorized
    @Operation(summary = "Create a new supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> create(@Valid @RequestBody SupplierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(dto));
    }

    @Authorized
    @Operation(summary = "Update supplier address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/address")
    public ResponseEntity<SupplierResponseDTO> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(supplierService.updateAddress(id, dto));
    }

    @Authorized
    @Operation(summary = "Delete supplier by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Authorized
    @Operation(summary = "Get all suppliers")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = SupplierResponseDTO.class))))
    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> findAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @Authorized
    @Operation(summary = "Get supplier by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }
}
