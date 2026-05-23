package com.auction.backend.service.service.impl;

import com.auction.backend.dto.*;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.Bid;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSessionMapper;
import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.readmodel.CustomerAuctionSessionReadModel;
import com.auction.backend.repository.AuctionSessionReadRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.BidRepository;
import com.auction.backend.service.AuctionSessionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSessionQueryServiceImpl implements AuctionSessionQueryService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionReadRepository auctionSessionReadRepository;
    private final AuctionSessionMapper auctionSessionMapper;
    private final BidRepository bidRepository;

    @Override
    public AuctionSessionDetailResponse getSessionDetail(String sessionId) {
        AuctionSessionDetailReadModel detail =
                auctionSessionReadRepository.findSessionDetailById(sessionId)
                        .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));

        return AuctionSessionDetailResponse.builder()
                .id(detail.getId())
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
                .currentLeaderName(detail.getCurrentLeaderName())
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

    @Override
    public PageResponse<CustomerAuctionSessionResponse> getCustomerSessions(SearchSessionRequest request) {
        Page<CustomerAuctionSessionReadModel> pageResult = auctionSessionReadRepository.searchAuctionSessionsDynamic(request);
        log.info("Found {} AuctionSessions", pageResult.getContent().size());
        List<CustomerAuctionSessionResponse> content = pageResult.getContent().stream()
                .map(auctionSessionMapper::toResponseForCustomer)
                .collect(Collectors.toList());

        return PageResponse.of(pageResult, content);
    }

    @Override
    public void validatePublicStreamAccess(String sessionId) {
        AuctionSession session = auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));

        if (!isPublicVisibleStatus(session.getStatus())) {
            throw new AppException("Phiên đấu giá không khả dụng để theo dõi");
        }
    }

    private boolean isPublicVisibleStatus(AuctionSessionStatus status) {
        return status == AuctionSessionStatus.SCHEDULED
                || status == AuctionSessionStatus.ACTIVE
                || status == AuctionSessionStatus.PAUSED
                || status == AuctionSessionStatus.ENDED
                || status == AuctionSessionStatus.FAILED;
    }

    private AuctionSession getSession(String sessionId) {
        return auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Không tìm thấy phiên đấu giá"));
    }
}
