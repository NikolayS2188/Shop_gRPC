package com.karkaror.shop.service;

import com.karkaror.shop.repository.ClientRepository;
import com.karkaror.shop.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Проверяет, используется ли адрес хотя бы одной сущностью (клиентом или поставщиком).
 * Нужен для безопасного удаления данных из адресов после смены/удаления владельца.
 * <p>Проверка идёт на стороне БД через {@code EXISTS}-запросы,
 *    без загрузки сущностей в память.
 */
@Component
@RequiredArgsConstructor
public class AddressUsageChecker {

    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;

    /**
     * @return true, если на адрес ссылается хотя бы один клиент или поставщик.
     */
    public boolean isUsed(UUID addressId) {
        if (addressId == null) {
            return false;
        }
        return clientRepository.existsByAddressId(addressId)
                || supplierRepository.existsByAddressId(addressId);
    }
}
