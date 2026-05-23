package com.auction.backend.controller;

import com.auction.backend.dto.*;
import com.auction.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auction-sessions")
@RequiredArgsConstructor
public class AuctionSessionController {

    private final AuctionSessionService auctionSessionService;
    private final AuctionSessionLifecycleService auctionSessionLifecycleService;
    private final AuctionSessionQueryService auctionSessionQueryService;
    private final AuctionSessionStatusHistoryService auctionSessionStatusHistoryService;
    private final AuctionSessionRealtimeService auctionSessionRealtimeService;

    @PostMapping
    public ResponseEntity<AuctionSessionResponse> createSession(@RequestBody @Valid CreateAuctionSessionRequest request) {
        return ResponseEntity.ok(auctionSessionService.createSession(request));
    }

    @PostMapping("/customer")
    public ResponseEntity<PageResponse<CustomerAuctionSessionResponse>> getCustomerSessions(
            @RequestBody SearchSessionRequest request
    ) {
        return ResponseEntity.ok(auctionSessionQueryService.getCustomerSessions(request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AuctionSessionResponse> activateSession(
            @PathVariable String id) {
        return ResponseEntity.ok(auctionSessionLifecycleService.activateSession(id));
    }

    @PatchMapping("/{id}/pause-or-resume")
    public ResponseEntity<AuctionSessionResponse> pauseOrResumeSession(
            @PathVariable String id,
            @RequestBody @Valid SessionLifecycleRequest request
    ) {
        return ResponseEntity.ok(auctionSessionLifecycleService.pauseOrResumeSession(id, request));
    }

    @PatchMapping("/{id}/fail")
    public ResponseEntity<AuctionSessionResponse> failSession(
            @PathVariable String id,
            @RequestBody @Valid SessionLifecycleRequest request
    ) {
        return ResponseEntity.ok(auctionSessionLifecycleService.failSession(id, request));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<AuctionSessionResponse> endSession(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(auctionSessionLifecycleService.endSession(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionSessionDetailResponse> getSession(@PathVariable String id) {
        return ResponseEntity.ok(auctionSessionQueryService.getSessionDetail(id));
    }

    @GetMapping("/{id}/bids")
    public ResponseEntity<List<BidHistoryItemResponse>> getBidHistory(@PathVariable String id) {
        return ResponseEntity.ok(auctionSessionQueryService.getBidHistory(id));
    }

    @PostMapping("/status-history/search")
    public ResponseEntity<PageResponse<AuctionSessionStatusHistoryResponse>> getStatusHistory(@RequestBody @Valid SearchSessionStatusHistoryRequest request) {
        return ResponseEntity.ok(auctionSessionStatusHistoryService.getHistoryBySessionId(request));
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAuctionSession(@PathVariable String id) {
        auctionSessionQueryService.validatePublicStreamAccess(id);
        return auctionSessionRealtimeService.subscribe(id);
    }
}