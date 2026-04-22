package com.auction.backend.controller;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    public CurrentWalletResponse getCurrentWallet() {
        return walletService.getCurrentWallet();
    }
}
