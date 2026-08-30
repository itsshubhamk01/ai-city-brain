package com.aicitybrain.repository;

import com.aicitybrain.domain.SimulationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SimulationRunRepository extends JpaRepository<SimulationRun, UUID> {
    List<SimulationRun> findTop20ByOrderByCreatedAtDesc();
}
