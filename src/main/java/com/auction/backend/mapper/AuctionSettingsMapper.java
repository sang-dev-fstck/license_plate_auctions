package com.auction.backend.mapper;

import com.auction.backend.dto.AuctionSettingsRequest;
import com.auction.backend.dto.AuctionSettingsResponse;
import com.auction.backend.dto.UpdateAuctionSettingsRequest;
import com.auction.backend.entity.AuctionSettings;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AuctionSettingsMapper {
    AuctionSettings toEntity(AuctionSettingsRequest request);

    AuctionSettingsResponse toResponse(AuctionSettings entity);

    // THÊM HÀM NÀY CHO API UPDATE
    // Nó sẽ lấy dữ liệu từ 'request' và ghi đè thẳng vào 'entity'


    // 1. Chỉ update những trường có dữ liệu, trường nào null thì giữ nguyên giá trị cũ trong DB
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // 2. Tuyệt đối KHÔNG BAO GIỜ cho phép update trường ID (Khóa chính)
    @Mapping(target = "id", ignore = true)
    // Nếu entity của bạn có các trường Audit (ngày tạo, người tạo), cũng nên ignore luôn:
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateAuctionSettingsRequest request, @MappingTarget AuctionSettings entity);

    AuctionSettings cloneEntity(AuctionSettings entity);
}
