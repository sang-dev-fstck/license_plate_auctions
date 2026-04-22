package com.auction.backend.mapper;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface WalletMapper {
    CurrentWalletResponse toResponse(Wallet entity);
}
