package com.auction.backend.controller;

import com.auction.backend.dto.AuctionSettingsRequest;
import com.auction.backend.dto.AuctionSettingsResponse;
import com.auction.backend.dto.UpdateAuctionSettingsRequest;
import com.auction.backend.service.AuctionSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auction-settings")
@RequiredArgsConstructor
public class AuctionSettingsController {
    private final AuctionSettingsService auctionSettingsService;

    @GetMapping
    public List<AuctionSettingsResponse> getAll() {
        return auctionSettingsService.getAuctionSettings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionSettingsResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(auctionSettingsService.getAuctionSettingById(id));
    }

    @PostMapping
    public ResponseEntity<AuctionSettingsResponse> save(@RequestBody AuctionSettingsRequest request) {
        return ResponseEntity.ok(auctionSettingsService.addAuctionSetting(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AuctionSettingsResponse> update(
            @RequestBody UpdateAuctionSettingsRequest request,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(auctionSettingsService.updateAuctionSetting(request, id));
    }
}
