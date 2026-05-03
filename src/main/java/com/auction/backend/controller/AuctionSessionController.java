package com.auction.backend.controller;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;
import com.auction.backend.dto.CustomerAuctionSessionResponse;
import com.auction.backend.service.AuctionSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auction-sessions")
@RequiredArgsConstructor
public class AuctionSessionController {

    private final AuctionSessionService auctionSessionService;

    @PostMapping
    public ResponseEntity<AuctionSessionResponse> createSession(@RequestBody @Valid CreateAuctionSessionRequest request) {
        return ResponseEntity.ok(auctionSessionService.createSession(request));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<CustomerAuctionSessionResponse>> getCustomerSessions() {
        return ResponseEntity.ok(auctionSessionService.getCustomerSessions());
    }
}