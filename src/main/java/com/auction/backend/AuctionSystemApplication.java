package com.auction.backend;

import com.auction.backend.repository.LicensePlateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
@EnableMongoAuditing
@RequiredArgsConstructor
public class AuctionSystemApplication implements CommandLineRunner {

    private final LicensePlateRepository repository;
    private final MongoTemplate mongoTemplate; // Thêm máy dò này vào

    public static void main(String[] args) {
        SpringApplication.run(AuctionSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("----- KIỂM TRA KẾT NỐI MONGODB -----");

        // 1. Soi xem nó đang chọc vào Database tên là gì?
        System.out.println("=> Tên Database đang kết nối: " + mongoTemplate.getDb().getName());

        // 2. Soi xem trong Database đó đang có những bảng (collection) nào?
        System.out.println("=> Danh sách các Collection hiện có: " + mongoTemplate.getCollectionNames());

        // 3. Đếm số lượng của Repository
        long count = repository.count();
        System.out.println("=> Số lượng biển số theo Repository: " + count);

        System.out.println("----- KẾT NỐI THÀNH CÔNG -----");
    }
}