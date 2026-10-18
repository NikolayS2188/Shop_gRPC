package com.karkaror.shop.repository;

import com.karkaror.shop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByAddressId(UUID addressId);
}
