package com.auction.backend.mapper;

import com.auction.backend.dto.TagRuleRequest;
import com.auction.backend.dto.TagRuleResponse;
import com.auction.backend.dto.UpdateTagRuleRequest;
import com.auction.backend.entity.TagRule;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TagRuleMapper {
    TagRule toEntity(TagRuleRequest request);

    TagRuleResponse toResponse(TagRule entity);

    // THÊM HÀM NÀY CHO API UPDATE
    // Nó sẽ lấy dữ liệu từ 'request' và ghi đè thẳng vào 'entity'


    // 1. Chỉ update những trường có dữ liệu, trường nào null thì giữ nguyên giá trị cũ trong DB
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // 2. Tuyệt đối KHÔNG BAO GIỜ cho phép update trường ID (Khóa chính)
    @Mapping(target = "id", ignore = true)
    // Nếu entity của bạn có các trường Audit (ngày tạo, người tạo), cũng nên ignore luôn:
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateTagRuleRequest request, @MappingTarget TagRule entity);

    TagRule cloneEntity(TagRule entity);
}
