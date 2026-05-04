package com.auction.backend.service;

import com.auction.backend.dto.PlaceBidRequest;
import com.auction.backend.dto.PlaceBidResponse;

public interface BidService {
    PlaceBidResponse placeBid(PlaceBidRequest request);
}