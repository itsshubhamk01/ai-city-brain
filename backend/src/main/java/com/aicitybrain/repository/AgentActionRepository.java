package com.aicitybrain.repository;

import com.aicitybrain.domain.AgentAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentActionRepository extends JpaRepository<AgentAction, UUID> {
    List<AgentAction> findTop50ByOrderByCreatedAtDesc();
}
