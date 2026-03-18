package com.auction.backend.mapper;

import com.auction.backend.dto.LicensePlateRequest;
import com.auction.backend.dto.LicensePlateResponse;
import com.auction.backend.entity.LicensePlate;
import org.mapstruct.Mapper;

// componentModel = "spring": Giúp Spring hiểu đây là 1 Bean,
// để bạn có thể @Autowired vào Controller như bình thường.
@Mapper(componentModel = "spring")
public interface LicensePlateMapper {
    // 1. Chuyển từ Request -> Entity
    // MapStruct tự động khớp các trường trùng tên (plateNumber -> plateNumber)
    // Nếu có trường nào khác tên, dùng @Mapping. Ví dụ:
    // @Mapping(target = "status", constant = "AVAILABLE") // Mặc định luôn set là AVAILABLE
    // @Mapping(target = "id", ignore = true) // Bỏ qua ID (vì tạo mới chưa có ID)
    LicensePlate toEntity(LicensePlateRequest request);

    // 2. Chuyển từ Entity -> Response
    // MapStruct tự động copy hết.
    LicensePlateResponse toResponse(LicensePlate entity);
}
