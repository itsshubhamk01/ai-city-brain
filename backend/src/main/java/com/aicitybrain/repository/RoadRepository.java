package com.aicitybrain.repository;

import com.aicitybrain.domain.Road;
import com.aicitybrain.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoadRepository extends JpaRepository<Road, UUID> {
    List<Road> findByZone(Zone zone);
}
