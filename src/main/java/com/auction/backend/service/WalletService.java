package com.auction.backend.service;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.dto.DepositRequest;
import com.auction.backend.dto.FreezeRequest;

public interface WalletService {
    CurrentWalletResponse getCurrentWallet();

    CurrentWalletResponse deposit(DepositRequest depositRequest);

    CurrentWalletResponse freeze(FreezeRequest freezeRequest);
}
