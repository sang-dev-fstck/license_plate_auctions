package com.auction.backend.repository;

import java.math.BigDecimal;

public interface WalletAtomicRepository {
    void freezeAvailable(String accountId, BigDecimal amount);

    void releaseFrozen(String accountId, BigDecimal amount);

    void debitFrozen(String accountId, BigDecimal amount);

    void creditAvailable(String accountId, BigDecimal amount);
}
