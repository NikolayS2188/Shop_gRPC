package com.karkaror.shop.service;

import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.ClientRequestDTO;
import com.karkaror.shop.dto.response.ClientResponseDTO;
import com.karkaror.shop.entity.Address;
import com.karkaror.shop.entity.Client;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления клиентами.
 * <p>Отвечает за CRUD-операции над сущностью {@link Client}
 *    и за корректную работу с его адресом ({@link Address}).</p>
 * <p>Создание клиента всегда создаёт новый адрес.
 *    При удалении клиента или смене адреса старый адрес удаляется,
 *    если на него больше никто не ссылается ({@link AddressUsageChecker}).</p>
 */
public interface ClientService {
    ClientResponseDTO create(ClientRequestDTO dto);
    void delete(UUID id);
    List<ClientResponseDTO> findByNameAndSurname(String name, String surname);
    List<ClientResponseDTO> findAll(Integer limit, Integer offset);
    ClientResponseDTO updateAddress(UUID id, AddressRequestDTO dto);
}
