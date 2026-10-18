package com.karkaror.shop.controller;

import com.karkaror.shop.annotation.Authorized;
import com.karkaror.shop.dto.response.ErrorResponse;
import com.karkaror.shop.dto.response.ImageResponseDTO;
import com.karkaror.shop.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image management endpoints")
public class ImageController {

    private final ImageService imageService;

    // addImage → @Authorized, возвращает ImageResponseDTO
    @Authorized
    @Operation(summary = "Add image to product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> addImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file) throws IOException {
        UUID id = imageService.addImageToProduct(productId, file.getBytes());
        return ResponseEntity.ok(ImageResponseDTO.builder().imageId(id).build());
    }

    @Authorized
    @Operation(summary = "Replace image by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> updateImage(
            @PathVariable UUID imageId,
            @RequestParam("file") MultipartFile file) throws IOException {
        UUID id = imageService.updateImage(imageId, file.getBytes());
        return ResponseEntity.ok(ImageResponseDTO.builder().imageId(id).build());
    }

    @Authorized
    @Operation(summary = "Delete image by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @Authorized
    @Operation(summary = "Get image of a product (attachment)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image bytes",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "404", description = "Product or image not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getImageByProduct(@PathVariable UUID productId) {
        byte[] bytes = imageService.getImageByProductId(productId);
        return ResponseEntity.ok()
                // Content-Disposition: attachment заставляет браузер скачать файл,
                // а не отобразить его inline.
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"image-" + productId + ".bin\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @Authorized
    @Operation(summary = "Get image by id (attachment)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image bytes",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{imageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getImageById(@PathVariable UUID imageId) {
        byte[] bytes = imageService.getImageById(imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"image-" + imageId + ".bin\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
