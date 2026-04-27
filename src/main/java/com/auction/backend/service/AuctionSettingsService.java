package com.auction.backend.service;

import com.auction.backend.dto.AuctionSettingsRequest;
import com.auction.backend.dto.AuctionSettingsResponse;
import com.auction.backend.dto.UpdateAuctionSettingsRequest;

import java.util.List;

public interface AuctionSettingsService {
    List<AuctionSettingsResponse> getAuctionSettings();

    AuctionSettingsResponse getAuctionSettingById(String id);

    AuctionSettingsResponse addAuctionSetting(AuctionSettingsRequest request);

    AuctionSettingsResponse updateAuctionSetting(UpdateAuctionSettingsRequest request, String id);
}
