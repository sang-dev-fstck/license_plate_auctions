package com.auction.backend;

import com.auction.backend.repository.LicensePlateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
@RequiredArgsConstructor
// implements CommandLineRunner: Giúp chạy code ngay khi server khởi động xong
public class AuctionSystemApplication implements CommandLineRunner {
    private final LicensePlateRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(AuctionSystemApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("----- KIỂM TRA KẾT NỐI MONGODB -----");
        long count = repository.count(); // Đếm số lượng bản ghi
        System.out.println("Số lượng biển số hiện có: " + count);
        System.out.println("----- KẾT NỐI THÀNH CÔNG -----");
    }
}
