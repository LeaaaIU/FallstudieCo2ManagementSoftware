package de.co2management.backend.entity;

import de.co2management.backend.enums.KategorieEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emission_eintrag")
@Data
public class EmissionEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private KategorieEnum kategorie;

    private Double wertCo2Kg;
    private String beschreibung;
    private LocalDate datum;
    private LocalDateTime erfasstAm;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    private Standort standort;

    @ManyToOne
    @JoinColumn(name = "erfasst_von_id")
    private Benutzer erfasstVon;
}
