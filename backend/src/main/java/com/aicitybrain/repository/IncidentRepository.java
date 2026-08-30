package com.aicitybrain.repository;

import com.aicitybrain.domain.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findTop50ByOrderByCreatedAtDesc();
    List<Incident> findByStatusNotOrderByCreatedAtDesc(Incident.Status status);
    long countByStatusNot(Incident.Status status);
    boolean existsByZoneAndTypeAndStatusNot(com.aicitybrain.domain.Zone zone, Incident.Type type, Incident.Status status);
    List<Incident> findByStatusNotAndAssignedAgentIsNull(Incident.Status status);
}
