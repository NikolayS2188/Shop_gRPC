package com.karkaror.shop.mapper;

import com.karkaror.shop.dto.response.SupplierResponseDTO;
import com.karkaror.shop.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplierMapper {

    private final AddressMapper addressMapper;

    public SupplierResponseDTO toResponse(Supplier entity) {
        if (entity == null) return null;
        return SupplierResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .address(addressMapper.toResponse(entity.getAddress()))
                .build();
    }
}
