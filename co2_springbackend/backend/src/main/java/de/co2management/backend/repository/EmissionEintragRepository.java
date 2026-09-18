package de.co2management.backend.repository;

import de.co2management.backend.entity.EmissionEintrag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmissionEintragRepository extends JpaRepository<EmissionEintrag, Long> {
    List<EmissionEintrag> findByStandortId(Long standortId);

    List<EmissionEintrag> findByStandortIdAndDatumBetween(Long standortId, LocalDate von, LocalDate bis);
    
    //Zeitraumfilter ohne Standort
    List<EmissionEintrag> findByDatumBetween(LocalDate von, LocalDate bis);

    //Summe CO2 je Standort, für alle Standorte
    @Query("SELECT e.standort.name, SUM(e.wertCo2Kg) FROM EmissionEintrag e GROUP BY e.standort.name")
    List<Object[]> sumCo2ProStandort();

    //Summe CO2 für genau einen Standort
    @Query("SELECT e.standort.name, SUM(e.wertCo2Kg) FROM EmissionEintrag e " +
           "WHERE e.standort.id = :standortId GROUP BY e.standort.name")
    List<Object[]> sumCo2ProStandort(@Param("standortId") Long standortId);
}