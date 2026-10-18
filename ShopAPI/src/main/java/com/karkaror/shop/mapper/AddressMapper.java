package com.karkaror.shop.mapper;

import com.karkaror.shop.dto.request.AddressRequestDTO;
import com.karkaror.shop.dto.response.AddressResponseDTO;
import com.karkaror.shop.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequestDTO dto) {
        if (dto == null) return null;
        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .build();
    }

    public AddressResponseDTO toResponse(Address entity) {
        if (entity == null) return null;
        return AddressResponseDTO.builder()
                .id(entity.getId())
                .country(entity.getCountry())
                .city(entity.getCity())
                .street(entity.getStreet())
                .build();
    }
}
