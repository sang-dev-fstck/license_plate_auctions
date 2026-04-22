package com.auction.backend.controller;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.dto.DepositRequest;
import com.auction.backend.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    public CurrentWalletResponse getCurrentWallet() {
        return walletService.getCurrentWallet();
    }

    @PostMapping("/deposit")
    public ResponseEntity<CurrentWalletResponse> deposit(@RequestBody @Valid DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(request));
    }
}
