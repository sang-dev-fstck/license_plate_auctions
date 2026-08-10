package com.auction.backend.enums;

import lombok.Getter;

@Getter
public enum AuctionSessionStatus {
    SCHEDULED(false, true),
    ACTIVE(true, false),
    PAUSED(false, false),
    ENDED(false, false),
    FAILED(false, false),
    DRAFT(false, false),
    ENDING(false, false);

    private final boolean biddingAllowed;
    private final boolean reservationAllowed;

    AuctionSessionStatus(
            boolean biddingAllowed,
            boolean reservationAllowed
    ) {
        this.biddingAllowed = biddingAllowed;
        this.reservationAllowed = reservationAllowed;
    }

    public boolean allowsBidding() {
        return biddingAllowed;
    }

    public boolean allowsReservation() {
        return reservationAllowed;
    }
}