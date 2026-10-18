package com.karkaror.shop.repository;

import com.karkaror.shop.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByClientNameIgnoreCaseAndClientSurnameIgnoreCase(String name, String surname);

    boolean existsByAddressId(UUID addressId);
}
