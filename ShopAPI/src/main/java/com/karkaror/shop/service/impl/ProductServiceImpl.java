package com.karkaror.shop.service.impl;

import com.karkaror.shop.dto.request.ProductRequestDTO;
import com.karkaror.shop.dto.response.ProductResponseDTO;
import com.karkaror.shop.entity.Category;
import com.karkaror.shop.entity.Image;
import com.karkaror.shop.entity.Product;
import com.karkaror.shop.entity.Supplier;
import com.karkaror.shop.exception.BadRequestException;
import com.karkaror.shop.exception.ResourceNotFoundException;
import com.karkaror.shop.mapper.ProductMapper;
import com.karkaror.shop.repository.*;
import com.karkaror.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ImageRepository imageRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Supplier supplier = dto.getSupplierId() == null
                ? null
                : supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier with id " + dto.getSupplierId() + " not found"));

        Category category = categoryRepository.findByNameIgnoreCase(dto.getCategory())
                .orElseGet(() -> categoryRepository.save(Category.builder().name(dto.getCategory()).build()));

        Product product = Product.builder()
                .name(dto.getName())
                .category(category)
                .price(dto.getPrice())
                .availableStock(dto.getAvailableStock())
                .lastUpdateDate(LocalDateTime.now())
                .supplier(supplier)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO decreaseStock(UUID id, int amount) {
        if (amount <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        if (product.getAvailableStock() < amount) {
            throw new BadRequestException("Not enough stock: available=" + product.getAvailableStock() + ", requested=" + amount);
        }
        product.setAvailableStock(product.getAvailableStock() - amount);
        product.setLastUpdateDate(LocalDateTime.now());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAllAvailable() {
        return productRepository.findAllAvailableWithDetails().stream()     // JOIN FETCH
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        Image image = product.getImage();
        product.setImage(null);
        productRepository.delete(product);
        if (image != null) {
            imageRepository.delete(image);
        }
    }
}
