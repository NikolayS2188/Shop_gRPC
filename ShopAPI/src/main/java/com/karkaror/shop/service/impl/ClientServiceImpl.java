package com.karkaror.shop.service.impl;

import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.ClientRequestDTO;
import com.karkaror.shop.dto.response.ClientResponseDTO;
import com.karkaror.shop.entity.Address;
import com.karkaror.shop.entity.Client;
import com.karkaror.shop.exception.BadRequestException;
import com.karkaror.shop.exception.ResourceNotFoundException;
import com.karkaror.shop.mapper.AddressMapper;
import com.karkaror.shop.mapper.ClientMapper;
import com.karkaror.shop.repository.AddressRepository;
import com.karkaror.shop.repository.ClientRepository;
import com.karkaror.shop.service.AddressUsageChecker;
import com.karkaror.shop.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ClientMapper clientMapper;
    private final AddressMapper addressMapper;
    private final AddressUsageChecker addressUsageChecker;

    @Override
    public ClientResponseDTO create(ClientRequestDTO dto) {
        Address address = resolveAddress(dto.getAddressId(), dto.getAddress());

        Client client = Client.builder()
                .clientName(dto.getClientName())
                .clientSurname(dto.getClientSurname())
                .birthday(dto.getBirthday())
                .gender(dto.getGender())
                .registrationDate(LocalDateTime.now())
                .address(address)
                .build();
        return clientMapper.toResponse(clientRepository.save(client));
    }

    /**
     * Гибридное разрешение адреса:
     * - если передан addressId — берём существующий;
     * - если передан address — создаём новый addressId;
     * - если оба null или оба заданы — ошибка 400 (на уровне DTO уже XOR, но на всякий случай).
     */
    private Address resolveAddress(UUID addressId, AddressRequestDTO addressDto) {
        if (addressId != null && addressDto != null) {
            throw new BadRequestException("Provide either addressId or address, not both");
        }
        if (addressId != null) {
            return addressRepository.findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address with id " + addressId + " not found"));
        }
        if (addressDto != null) {
            return addressRepository.save(addressMapper.toEntity(addressDto));
        }
        throw new BadRequestException("Either addressId or address must be provided");
    }

    @Override
    public void delete(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " not found"));
        Address address = client.getAddress();
        clientRepository.delete(client);
        // удаляем адрес, если он больше никому не принадлежит
        if (address != null && !addressUsageChecker.isUsed(address.getId())) {
            addressRepository.delete(address);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDTO> findByNameAndSurname(String name, String surname) {
        return clientRepository.findByClientNameIgnoreCaseAndClientSurnameIgnoreCase(name, surname)
                .stream().map(clientMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDTO> findAll(Integer limit, Integer offset) {
        List<Client> clients;
        if (limit != null && offset != null) {
            Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), limit);
            clients = clientRepository.findAll(pageable).getContent();
        } else {
            clients = clientRepository.findAll();
        }
        return clients.stream().map(clientMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ClientResponseDTO updateAddress(UUID id, AddressRequestDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " not found"));
        Address oldAddress = client.getAddress();

        Address newAddress = addressRepository.save(addressMapper.toEntity(dto));
        client.setAddress(newAddress);
        clientRepository.save(client);

        if (oldAddress != null && !addressUsageChecker.isUsed(oldAddress.getId())) {
            addressRepository.delete(oldAddress);
        }
        return clientMapper.toResponse(client);
    }
}
