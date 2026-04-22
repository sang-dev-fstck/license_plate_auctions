package com.auction.backend.service;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.dto.DepositRequest;

public interface WalletService {
    CurrentWalletResponse getCurrentWallet();

    CurrentWalletResponse deposit(DepositRequest depositRequest);
}
