package com.karkaror.shop.service.impl;

import com.karkaror.shop.entity.Image;
import com.karkaror.shop.entity.Product;
import com.karkaror.shop.exception.ResourceNotFoundException;
import com.karkaror.shop.repository.ImageRepository;
import com.karkaror.shop.repository.ProductRepository;
import com.karkaror.shop.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;

    @Override
    public UUID addImageToProduct(UUID productId, byte[] bytes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found"));

        Image image = imageRepository.save(Image.builder().image(bytes).build());
        product.setImage(image);
        productRepository.save(product);
        return image.getId();
    }

    @Override
    public UUID updateImage(UUID imageId, byte[] bytes) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image with id " + imageId + " not found"));
        image.setImage(bytes);
        imageRepository.save(image);
        return image.getId();
    }

    @Override
    public void deleteImage(UUID imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image with id " + imageId + " not found"));
        // Перед удалением изображения отвязываем его от товаров,
        // иначе получим FK violation (product.image_id ссылается на images.id).
        productRepository.detachImage(imageId);             // @Modifying, без N+1
        imageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found"));
        if (product.getImage() == null) {
            throw new ResourceNotFoundException("Product " + productId + " has no image");
        }
        return product.getImage().getImage();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageById(UUID imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image with id " + imageId + " not found"));
        return image.getImage();
    }
}
