package com.auction.backend.service.service.impl;

import com.auction.backend.dto.CurrentWalletResponse;
import com.auction.backend.dto.DepositRequest;
import com.auction.backend.dto.FreezeRequest;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.Wallet;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.WalletMapper;
import com.auction.backend.repository.AccountRepository;
import com.auction.backend.repository.WalletAtomicRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final WalletAtomicRepository walletAtomicRepository;
    private final AccountRepository accountRepository;
    private final WalletMapper walletMapper;

    @Override
    public CurrentWalletResponse getCurrentWallet() {
        Wallet wallet = findCurrentWallet();
        log.info("Current Wallet Details: {}", wallet);
        return walletMapper.toResponse(wallet);
    }

    @Override
    public CurrentWalletResponse deposit(DepositRequest depositRequest) {
        long start = System.currentTimeMillis();

        Wallet wallet = findCurrentWallet();
        wallet.deposit(depositRequest.getAmount());
        Wallet updatedWallet = walletRepository.save(wallet);
        long end = System.currentTimeMillis();

        log.info("Deposit completed in {} ms", (end - start));
        return walletMapper.toResponse(updatedWallet);
    }

    @Override
    public CurrentWalletResponse freeze(FreezeRequest freezeRequest) {
        long start = System.currentTimeMillis();
        Wallet wallet = findCurrentWallet();
        String accountId = wallet.getAccountId();
        walletAtomicRepository.freezeAvailable(accountId, freezeRequest.getAmount());
        Wallet updatedWallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy ví"));
        long end = System.currentTimeMillis();
        log.info("Freeze completed in {} ms", (end - start));
        return walletMapper.toResponse(updatedWallet);
    }

    private Wallet findCurrentWallet() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("Người dùng chưa đăng nhập");
        }
        String username = authentication.getName();
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy người dùng"));
        String id = account.getId();
        return walletRepository.findByAccountId(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy ví"));
    }
}
