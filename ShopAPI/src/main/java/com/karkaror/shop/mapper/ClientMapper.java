package com.karkaror.shop.mapper;

import com.karkaror.shop.dto.response.ClientResponseDTO;
import com.karkaror.shop.entity.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMapper {

    private final AddressMapper addressMapper;

    public ClientResponseDTO toResponse(Client entity) {
        if (entity == null) return null;
        return ClientResponseDTO.builder()
                .id(entity.getId())
                .clientName(entity.getClientName())
                .clientSurname(entity.getClientSurname())
                .birthday(entity.getBirthday())
                .gender(entity.getGender())
                .registrationDate(entity.getRegistrationDate())
                .address(addressMapper.toResponse(entity.getAddress()))
                .build();
    }
}
