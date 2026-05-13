package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionDetailResponse;
import com.auction.backend.dto.BidHistoryItemResponse;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.Bid;
import com.auction.backend.exception.AppException;
import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.repository.AuctionSessionReadRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.BidRepository;
import com.auction.backend.service.AuctionSessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionSessionQueryServiceImpl implements AuctionSessionQueryService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionReadRepository auctionSessionReadRepository;
    private final BidRepository bidRepository;

    @Override
    public AuctionSessionDetailResponse getSessionDetail(String sessionId) {
        AuctionSessionDetailReadModel detail =
                auctionSessionReadRepository.findSessionDetailById(sessionId)
                        .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));

        return AuctionSessionDetailResponse.builder()
                .id(detail.getId())
                .licensePlateId(detail.getLicensePlateId())
                .licensePlateNumber(detail.getLicensePlateNumber())
                .categoryName(detail.getCategoryName())
                .provinceName(detail.getProvinceName())
                .tags(detail.getTags())
                .status(detail.getStatus())
                .startTime(detail.getStartTime())
                .endTime(detail.getEndTime())
                .startingPrice(detail.getStartingPrice())
                .currentPrice(detail.getCurrentPrice())
                .bidStepAmountSnapshot(detail.getBidStepAmountSnapshot())
                .currentLeaderAccountId(detail.getCurrentLeaderAccountId())
                .currentLeaderName(detail.getCurrentLeaderName())
                .winnerAccountId(detail.getWinnerAccountId())
                .winnerName(detail.getWinnerName())
                .pauseReason(detail.getPauseReason())
                .failureReason(detail.getFailureReason())
                .build();
    }

    @Override
    public List<BidHistoryItemResponse> getBidHistory(String sessionId) {
        AuctionSession session = getSession(sessionId);
        List<Bid> bids = bidRepository.findByAuctionSessionIdOrderByCreatedAtDesc(session.getId());
        return bids.stream()
                .map(bid -> BidHistoryItemResponse.builder()
                        .bidId(bid.getId())
                        .bidderAccountId(bid.getBidderAccountId())
                        .bidderName(bid.getBidderFullNameSnapshot() != null
                                ? bid.getBidderFullNameSnapshot()
                                : "Unknown User")
                        .amount(bid.getAmount())
                        .status(bid.getStatus())
                        .createdAt(bid.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private AuctionSession getSession(String sessionId) {
        return auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));
    }
}
