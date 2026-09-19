package de.co2management.backend.repository;

import de.co2management.backend.entity.Bericht;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface BerichtRepository extends JpaRepository<Bericht, Long> {
    List<Bericht> findByStandortId(Long standortId);
    
}
