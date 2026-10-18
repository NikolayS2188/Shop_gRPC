package com.karkaror.shop.mapper;

import com.karkaror.shop.dto.response.ProductResponseDTO;
import com.karkaror.shop.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponse(Product entity) {
        if (entity == null) return null;
        return ProductResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .price(entity.getPrice())
                .availableStock(entity.getAvailableStock())
                .lastUpdateDate(entity.getLastUpdateDate())
                .supplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null)
                .supplierName(entity.getSupplier() != null ? entity.getSupplier().getName() : null)
                .imageId(entity.getImage() != null ? entity.getImage().getId() : null)
                .build();
    }
}
