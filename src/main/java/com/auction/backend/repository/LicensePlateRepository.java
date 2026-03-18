package com.auction.backend.repository;

import com.auction.backend.entity.LicensePlate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicensePlateRepository extends MongoRepository<LicensePlate, String> {
    // Bên trong này đang trống, nhưng thực ra nó đã thừa hưởng hàng chục hàm xịn xò:
    // .save(entity) -> Lưu
    // .findAll() -> Lấy hết
    // .findById(id) -> Tìm theo ID
    // .deleteById(id) -> Xóa

    // Bạn có thể định nghĩa thêm hàm tìm kiếm theo ý muốn chỉ bằng cách đặt tên hàm:
    // Ví dụ: Tìm biển số theo Tỉnh
    // Spring sẽ tự dịch câu này thành lệnh MongoDB: db.license_plates.find({province: ?})
//    java.util.List<LicensePlate> findByProvince(String province);
}
