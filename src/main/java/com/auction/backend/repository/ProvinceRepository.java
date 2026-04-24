package com.auction.backend.repository;

import com.auction.backend.entity.Province;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends MongoRepository<Province, String> {
    Optional<Province> findFirstByLocalSymbolsContaining(String localSymbol);
}
