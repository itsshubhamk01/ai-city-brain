package com.aicitybrain.repository;

import com.aicitybrain.domain.WasteBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WasteBinRepository extends JpaRepository<WasteBin, UUID> {
    List<WasteBin> findByCapacityPctGreaterThanEqual(double threshold);
}
