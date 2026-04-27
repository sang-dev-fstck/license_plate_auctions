package com.auction.backend.mapper;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;
import com.auction.backend.entity.AuctionSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface AuctionSessionMapper {
    AuctionSession toEntity(CreateAuctionSessionRequest request);

    AuctionSessionResponse toResponse(AuctionSession entity);
}
