package com.karkaror.shop.service.impl;

import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.request.SupplierRequestDTO;
import com.karkaror.shop.dto.response.SupplierResponseDTO;
import com.karkaror.shop.entity.Address;
import com.karkaror.shop.entity.Supplier;
import com.karkaror.shop.exception.BadRequestException;
import com.karkaror.shop.exception.ResourceNotFoundException;
import com.karkaror.shop.mapper.AddressMapper;
import com.karkaror.shop.mapper.SupplierMapper;
import com.karkaror.shop.repository.AddressRepository;
import com.karkaror.shop.repository.SupplierRepository;
import com.karkaror.shop.service.AddressUsageChecker;
import com.karkaror.shop.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressRepository addressRepository;
    private final SupplierMapper supplierMapper;
    private final AddressMapper addressMapper;
    private final AddressUsageChecker addressUsageChecker;

    @Override
    public SupplierResponseDTO create(SupplierRequestDTO dto) {
        Address address = resolveAddress(dto.getAddressId(), dto.getAddress());

        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .address(address)
                .build();
        return supplierMapper.toResponse(supplierRepository.save(supplier));
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
    public SupplierResponseDTO updateAddress(UUID id, AddressRequestDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " not found"));
        Address oldAddress = supplier.getAddress();

        Address newAddress = addressRepository.save(addressMapper.toEntity(dto));
        supplier.setAddress(newAddress);
        supplierRepository.save(supplier);

        if (oldAddress != null && !addressUsageChecker.isUsed(oldAddress.getId())) {
            addressRepository.delete(oldAddress);
        }
        return supplierMapper.toResponse(supplier);
    }

    @Override
    public void delete(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " not found"));
        Address address = supplier.getAddress();
        supplierRepository.delete(supplier);
        // удаляем адрес, если он больше никому не принадлежит
        if (address != null && !addressUsageChecker.isUsed(address.getId())) {
            addressRepository.delete(address);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> findAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " not found"));
        return supplierMapper.toResponse(supplier);
    }
}
