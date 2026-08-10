package com.auction.backend.service.impl;

import com.auction.backend.dto.PlaceBidRequest;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.exception.RateLimitExceededException;
import com.auction.backend.repository.*;
import com.auction.backend.security.CurrentAccountProvider;
import com.auction.backend.security.ratelimit.RateLimiterService;
import com.auction.backend.service.AuctionSessionCacheService;
import com.auction.backend.service.AuctionSessionRealtimeService;
import com.auction.backend.service.policy.BidAmountPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {
    @Spy
    private final BidAmountPolicy bidAmountPolicy =
            new BidAmountPolicy();
    @Mock
    private BidRepository bidRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletAtomicRepository walletAtomicRepository;
    @Mock
    private CurrentAccountProvider currentAccountProvider;
    @Mock
    private AuctionSessionRepository auctionSessionRepository;
    @Mock
    private AuctionParticipationRepository auctionParticipationRepository;
    @Mock
    private AuctionSessionAtomicRepository auctionSessionAtomicRepository;
    @Mock
    private AuctionSessionRealtimeService auctionSessionRealtimeService;
    @Mock
    private AuctionSessionCacheService auctionSessionCacheService;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private BidServiceImpl bidService;


    @Test
    void shouldRejectBidBelowMinimumBeforeAnyMutation() {
        String accountId = "account-1";
        String sessionId = "session-1";

        BigDecimal currentPrice = new BigDecimal("100");
        BigDecimal bidStep = new BigDecimal("10");
        BigDecimal submittedAmount = new BigDecimal("109");

        Account account = Account.builder()
                .id(accountId)
                .fullName("Sang")
                .build();

        AuctionSession session = AuctionSession.builder()
                .id(sessionId)
                .currentPrice(currentPrice)
                .status(AuctionSessionStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now().plusMinutes(10))
                .bidStepAmountSnapshot(bidStep)
                .build();

        AuctionParticipation participation =
                AuctionParticipation.builder()
                        .id("participation-1")
                        .auctionSessionId(sessionId)
                        .accountId(accountId)
                        .status(ParticipationStatus.RESERVED)
                        .depositAmount(new BigDecimal("50"))
                        .build();

        PlaceBidRequest request =
                new PlaceBidRequest(sessionId, submittedAmount);

        when(currentAccountProvider.getCurrentAccount())
                .thenReturn(account);

        when(auctionSessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(auctionParticipationRepository
                .findByAuctionSessionIdAndAccountId(sessionId, accountId))
                .thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("110");

        verify(bidAmountPolicy).validate(
                submittedAmount,
                currentPrice,
                bidStep
        );

        verify(walletAtomicRepository, never())
                .freezeAvailable(
                        anyString(),
                        any(BigDecimal.class)
                );

        verify(auctionParticipationRepository, never())
                .save(any(AuctionParticipation.class));

        verifyNoInteractions(
                auctionSessionAtomicRepository,
                bidRepository,
                auctionSessionRealtimeService,
                auctionSessionCacheService
        );
    }

    @Test
    void shouldStopFlowWhenAuctionSessionDoesNotExist() {
        String accountId = "account-1";
        String sessionId = "missing-session";
        Account account = Account.builder()
                .id(accountId)
                .fullName("Sang")
                .build();

        PlaceBidRequest request = new PlaceBidRequest(
                sessionId,
                new BigDecimal("110")
        );

        when(currentAccountProvider.getCurrentAccount())
                .thenReturn(account);

        when(auctionSessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> bidService.placeBid(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Phiên đấu giá không hợp lệ hoặc không tồn tại");

        verify(auctionSessionRepository)
                .findById(sessionId);

        verifyNoInteractions(
                auctionParticipationRepository,
                bidAmountPolicy,
                walletRepository,
                walletAtomicRepository,
                auctionSessionAtomicRepository,
                bidRepository,
                auctionSessionRealtimeService,
                auctionSessionCacheService
        );
    }

    @Test
    void shouldStopFlowBeforeDatabaseAccessWhenBidRateLimitExceeded() {
        // Arrange
        String accountId = "account-1";
        String sessionId = "session-1";

        Account account = Account.builder()
                .id(accountId)
                .fullName("Sang")
                .build();

        PlaceBidRequest request = new PlaceBidRequest(
                sessionId,
                new BigDecimal("110")
        );

        when(currentAccountProvider.getCurrentAccount())
                .thenReturn(account);

        RateLimitExceededException expectedException =
                new RateLimitExceededException(
                        "Bạn đang gửi giá quá nhanh, vui lòng thử lại sau",
                        Duration.ofSeconds(3)
                );

        doThrow(expectedException)
                .when(rateLimiterService)
                .checkBidLimit(accountId, sessionId);

        // Act + Assert
        assertThatThrownBy(() -> bidService.placeBid(request))
                .isSameAs(expectedException);

        verify(currentAccountProvider)
                .getCurrentAccount();

        verify(rateLimiterService)
                .checkBidLimit(accountId, sessionId);

        verifyNoInteractions(
                auctionSessionRepository,
                auctionParticipationRepository,
                bidAmountPolicy,
                walletRepository,
                walletAtomicRepository,
                auctionSessionAtomicRepository,
                bidRepository,
                auctionSessionRealtimeService,
                auctionSessionCacheService
        );
    }
}
