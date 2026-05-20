package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionStatusHistoryResponse;
import com.auction.backend.dto.PageResponse;
import com.auction.backend.dto.SearchSessionStatusHistoryRequest;
import com.auction.backend.entity.AuctionSessionStatusHistory;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.StatusChangedByType;
import com.auction.backend.mapper.AuctionSessionStatusHistoryMapper;
import com.auction.backend.readmodel.AuctionSessionStatusHistoryReadModel;
import com.auction.backend.repository.AuctionSessionStatusHistoryReadRepository;
import com.auction.backend.repository.AuctionSessionStatusHistoryRepository;
import com.auction.backend.service.AuctionSessionStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuctionSessionStatusHistoryServiceImpl implements AuctionSessionStatusHistoryService {
    private final AuctionSessionStatusHistoryReadRepository readHistoryRepository;
    private final AuctionSessionStatusHistoryRepository commandHistoryRepository;
    private final AuctionSessionStatusHistoryMapper historyMapper;

    @Override
    public void recordStatusChange(String sessionId, AuctionSessionStatus fromStatus, AuctionSessionStatus toStatus, String reason, StatusChangedByType changedByType, String changedByAccountId) {
        if (sessionId == null || fromStatus == null || toStatus == null) {
            log.warn(
                    "Skip recording auction session status history due to invalid data. sessionId={}, fromStatus={}, toStatus={}",
                    sessionId,
                    fromStatus,
                    toStatus
            );
            return;
        }

        if (fromStatus == toStatus) {
            return;
        }

        AuctionSessionStatusHistory auctionSessionStatusHistory = AuctionSessionStatusHistory.builder()
                .auctionSessionId(sessionId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reason(reason)
                .changedByType(changedByType)
                .changedByAccountId(changedByAccountId)
                .build();
        try {
            commandHistoryRepository.save(auctionSessionStatusHistory);
        } catch (Exception e) {
            log.error(
                    "Failed to record auction session status history. sessionId={}, fromStatus={}, toStatus={}",
                    sessionId,
                    fromStatus,
                    toStatus,
                    e
            );
        }
    }

    @Override
    public PageResponse<AuctionSessionStatusHistoryResponse> getHistoryBySessionId(SearchSessionStatusHistoryRequest request) {
        Page<AuctionSessionStatusHistoryReadModel> pageResult = readHistoryRepository.searchAuctionSessionHistoryDynamic(request);
        List<AuctionSessionStatusHistoryResponse> content = pageResult.getContent().stream()
                .map(historyMapper::toResponse)
                .collect(Collectors.toList());
        return PageResponse.of(pageResult, content);
    }
}
