package com.auction.backend.repository;

import com.auction.backend.entity.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByEmail(String email);

    Boolean existsByEmail(String email);

    Set<Account> findByIdIn(Set<String> emails);
}
