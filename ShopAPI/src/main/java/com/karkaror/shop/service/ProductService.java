package com.karkaror.shop.service;

import com.karkaror.shop.dto.request.ProductRequestDTO;
import com.karkaror.shop.dto.response.ProductResponseDTO;
import com.karkaror.shop.entity.Category;
import com.karkaror.shop.entity.Image;
import com.karkaror.shop.entity.Product;
import com.karkaror.shop.entity.Supplier;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления товарами.
 * <p>Отвечает за CRUD-операции над сущностью {@link Product},
 *    управление остатками и связями с {@link Category}, {@link Supplier}
 *    и {@link Image}.</p>
 * <p>Категория хранится в отдельной таблице (3НФ): если её нет — создаётся
 *    автоматически.</p>
 * <p>Изображение связано с товаром один-к-одному;
 *    при удалении товара изображение также удаляется.</p>
 * <p>Поставщик опционален — при его удалении
 *    СУБД обнуляет {@code product.supplier_id}.</p>
 */
public interface ProductService {
    ProductResponseDTO create(ProductRequestDTO dto);
    ProductResponseDTO decreaseStock(UUID id, int amount);
    ProductResponseDTO findById(UUID id);
    List<ProductResponseDTO> findAllAvailable();
    void delete(UUID id);
}
