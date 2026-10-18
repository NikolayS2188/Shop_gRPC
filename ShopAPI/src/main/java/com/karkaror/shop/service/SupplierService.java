package com.karkaror.shop.service;

import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.SupplierRequestDTO;
import com.karkaror.shop.dto.response.SupplierResponseDTO;
import com.karkaror.shop.entity.Address;
import com.karkaror.shop.entity.Supplier;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления поставщиками.
 * <p>Отвечает за CRUD-операции над сущностью {@link Supplier}
 *    и за корректную работу с его адресом ({@link Address}).</p>
 * <p>Создание поставщика всегда создаёт новый адрес.
 *    При удалении поставщика или смене адреса старый адрес удаляется,
 *    если на него больше никто не ссылается ({@link AddressUsageChecker}).</p>
 * <p>Товары поставщика не удаляются вместе с ним: СУБД обнуляет
 *    {@code product.supplier_id} по правилу {@code ON DELETE SET NULL}.</p>
 */
public interface SupplierService {
    SupplierResponseDTO create(SupplierRequestDTO dto);
    SupplierResponseDTO updateAddress(UUID id, AddressRequestDTO dto);
    void delete(UUID id);
    List<SupplierResponseDTO> findAll();
    SupplierResponseDTO findById(UUID id);
}
