package com.aicitybrain.repository;

import com.aicitybrain.domain.City;
import com.aicitybrain.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    List<Zone> findByCity(City city);
    List<Zone> findByCityOrderByNameAsc(City city);
    java.util.Optional<Zone> findByNameIgnoreCase(String name);
}
