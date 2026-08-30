package com.aicitybrain.repository;

import com.aicitybrain.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findTop50ByOrderByCreatedAtDesc();
    long countByAcknowledgedFalseAndSeverityIn(List<com.aicitybrain.domain.Severity> severities);
}
