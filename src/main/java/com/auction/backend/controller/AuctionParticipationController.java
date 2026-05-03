package com.auction.backend.controller;

import com.auction.backend.dto.JoinAuctionSessionRequest;
import com.auction.backend.dto.JoinAuctionSessionResponse;
import com.auction.backend.service.AuctionParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auction-participations")
@RequiredArgsConstructor
public class AuctionParticipationController {

    private final AuctionParticipationService auctionParticipationService;

    @PostMapping("/join")
    public ResponseEntity<JoinAuctionSessionResponse> joinSession(
            @RequestBody @Valid JoinAuctionSessionRequest request
    ) {
        return ResponseEntity.ok(auctionParticipationService.joinAuctionSession(request));
    }
}