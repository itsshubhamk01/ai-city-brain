package com.aicitybrain.repository;

import com.aicitybrain.domain.PowerStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PowerStationRepository extends JpaRepository<PowerStation, UUID> {
}
