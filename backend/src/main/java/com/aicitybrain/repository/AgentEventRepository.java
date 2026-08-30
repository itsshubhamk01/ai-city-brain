package com.aicitybrain.repository;

import com.aicitybrain.domain.AgentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentEventRepository extends JpaRepository<AgentEvent, UUID> {
    List<AgentEvent> findTop50ByOrderByCreatedAtDesc();
}
