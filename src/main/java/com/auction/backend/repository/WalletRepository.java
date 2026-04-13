package com.auction.backend.repository;

import com.auction.backend.entity.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends MongoRepository<Wallet, Long> {
    Optional<Wallet> findByAccountId(String accountId);
}
