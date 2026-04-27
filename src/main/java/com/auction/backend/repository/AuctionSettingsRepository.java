package com.auction.backend.repository;

import com.auction.backend.entity.AuctionSettings;
import com.auction.backend.enums.VehicleType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSettingsRepository extends MongoRepository<AuctionSettings, String> {
    Optional<AuctionSettings> findByVehicleTypeAndActiveTrue(VehicleType vehicleType);

    boolean existsByVehicleTypeAndActiveTrue(VehicleType vehicleType);
}
