package com.aicitybrain.repository;

import com.aicitybrain.domain.FireStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FireStationRepository extends JpaRepository<FireStation, UUID> {
}
