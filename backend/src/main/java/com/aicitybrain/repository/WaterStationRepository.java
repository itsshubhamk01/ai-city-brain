package com.aicitybrain.repository;

import com.aicitybrain.domain.WaterStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaterStationRepository extends JpaRepository<WaterStation, UUID> {
}
