package com.karkaror.shop.service;

import java.util.UUID;

/**
 * Сервис управления изображениями товаров.
 * <p> Удаление изображения предварительно отвязывает его от всех товаров
 *     (обнуляет {@code product.image_id}), чтобы избежать нарушения
 *     внешнего ключа.</p>
 * <p> Методы чтения возвращают сырой {@code byte[]} — контроллер отдаёт
 *     его с заголовком {@code application/octet-stream} и
 *     {@code Content-Disposition: attachment} для автоматического
 *     скачивания файла браузером.</p>
 */
public interface ImageService {
    UUID addImageToProduct(UUID productId, byte[] bytes);
    UUID updateImage(UUID imageId, byte[] bytes);
    void deleteImage(UUID imageId);
    byte[] getImageByProductId(UUID productId);
    byte[] getImageById(UUID imageId);
}
