package com.auction.backend.mapper;

import com.auction.backend.dto.AccountResponse;
import com.auction.backend.dto.RegisterRequest;
import com.auction.backend.dto.UpdateAccountRequest;
import com.auction.backend.entity.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toEntity(RegisterRequest request);

    AccountResponse toResponse(Account entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // 2. Tuyệt đối KHÔNG BAO GIỜ cho phép update trường ID (Khóa chính)
    @Mapping(target = "id", ignore = true)
    // Nếu entity của bạn có các trường Audit (ngày tạo, người tạo), cũng nên ignore luôn:
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateAccountRequest request, @MappingTarget Account entity);

}
