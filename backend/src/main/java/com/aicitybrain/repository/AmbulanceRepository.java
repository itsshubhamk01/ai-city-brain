package com.aicitybrain.repository;

import com.aicitybrain.domain.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AmbulanceRepository extends JpaRepository<Ambulance, UUID> {
    List<Ambulance> findByStatus(Ambulance.Status status);
}
