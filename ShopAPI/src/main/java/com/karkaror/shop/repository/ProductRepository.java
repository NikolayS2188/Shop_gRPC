package com.karkaror.shop.repository;

import com.karkaror.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("select p from Product p left join fetch p.category left join fetch p.supplier where p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") UUID id);

    /** Оптимизированный запрос: JOIN FETCH + фильтр по stock. Устраняет N+1. */
    @Query("select p from Product p " +
            "left join fetch p.category " +
            "left join fetch p.supplier " +
            "where p.availableStock > 0")
    List<Product> findAllAvailableWithDetails();

    /** Отвязывает изображение от товара перед удалением (устраняет N+1). */
    @Modifying
    @Query("update Product p set p.image = null where p.image.id = :imageId")
    void detachImage(@Param("imageId") UUID imageId);
}
