package com.auction.backend.mapper;

import com.auction.backend.dto.AuctionSessionStatusHistoryResponse;
import com.auction.backend.readmodel.AuctionSessionStatusHistoryReadModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface AuctionSessionStatusHistoryMapper {
    AuctionSessionStatusHistoryResponse toResponse(AuctionSessionStatusHistoryReadModel entity);

}
