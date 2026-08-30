package com.aicitybrain.repository;

import com.aicitybrain.domain.Hospital;
import com.aicitybrain.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {
    List<Hospital> findByZone(Zone zone);
}
