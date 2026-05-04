package com.auction.backend.controller;

import com.auction.backend.dto.PlaceBidRequest;
import com.auction.backend.dto.PlaceBidResponse;
import com.auction.backend.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<PlaceBidResponse> placeBid(@RequestBody @Valid PlaceBidRequest request) {
        return ResponseEntity.ok(bidService.placeBid(request));
    }
}