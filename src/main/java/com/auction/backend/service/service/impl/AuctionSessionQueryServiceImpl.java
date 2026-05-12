package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionDetailResponse;
import com.auction.backend.dto.BidHistoryItemResponse;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.Bid;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AccountRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.BidRepository;
import com.auction.backend.service.AuctionSessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuctionSessionQueryServiceImpl implements AuctionSessionQueryService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AccountRepository accountRepository;
    private final BidRepository bidRepository;

    @Override
    public AuctionSessionDetailResponse getSessionDetail(String sessionId) {
        AuctionSession session = getSession(sessionId);

        Set<String> accountIds = Stream.of(session.getCurrentLeaderAccountId(), session.getWinnerAccountId()).collect(Collectors.toSet());

        Map<String, Account> accountMap = accountRepository.findByIdIn(accountIds)
                .stream()
                .collect(Collectors.toMap(
                        Account::getId,
                        Function.identity()
                ));
        
        Account leader = accountMap.get(session.getCurrentLeaderAccountId());

        Account winner = accountMap.get(session.getWinnerAccountId());

        return AuctionSessionDetailResponse.builder()
                .id(session.getId())
                .licensePlateId(session.getLicensePlateId())
                .licensePlateNumber(session.getLicensePlateNumber())
                .status(session.getStatus())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .startingPrice(session.getStartingPrice())
                .currentPrice(session.getCurrentPrice())
                .bidStepAmountSnapshot(session.getBidStepAmountSnapshot())
                .currentLeaderAccountId(session.getCurrentLeaderAccountId())
                .currentLeaderName(leader.getFullName() != null ? leader.getFullName() : "Unknown User")
                .winnerAccountId(session.getWinnerAccountId())
                .winnerName(winner.getFullName() != null ? winner.getFullName() : "Unknown User")
                .pauseReason(session.getPauseReason())
                .failureReason(session.getFailureReason())
                .build();
    }

    @Override
    public List<BidHistoryItemResponse> getBidHistory(String sessionId) {
        AuctionSession session = getSession(sessionId);
        List<Bid> bids = bidRepository.findByAuctionSessionIdOrderByCreatedAtDesc(session.getId());

        Set<String> accountIds = bids.stream()
                .map(Bid::getBidderAccountId)
                .collect(Collectors.toSet());

        Map<String, Account> accountMap = accountRepository.findByIdIn(accountIds)
                .stream()
                .collect(Collectors.toMap(
                        Account::getId,
                        Function.identity()
                ));

        return bids.stream()
                .map(bid -> {
                    Account account = accountMap.get(bid.getBidderAccountId());
                    return BidHistoryItemResponse.builder()
                            .bidId(bid.getId())
                            .bidderAccountId(bid.getBidderAccountId())
                            .bidderName(account != null
                                    ? account.getFullName()
                                    : "Unknown User")
                            .amount(bid.getAmount())
                            .status(bid.getStatus())
                            .createdAt(bid.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private AuctionSession getSession(String sessionId) {
        return auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));
    }
}
