package com.aicitybrain.repository;

import com.aicitybrain.domain.Zone;
import com.aicitybrain.domain.ZoneStatusSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ZoneStatusSnapshotRepository extends JpaRepository<ZoneStatusSnapshot, UUID> {
    List<ZoneStatusSnapshot> findTop50ByZoneOrderByCreatedAtDesc(Zone zone);
}
