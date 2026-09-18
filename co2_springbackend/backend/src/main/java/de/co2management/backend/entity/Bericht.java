package de.co2management.backend.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bericht")
@Data

public class Bericht {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate zeitraumVon;
    private LocalDate zeitraumBis;
    private double gesamtCo2Kg;
    private LocalDate erstelltAm;
    private String dateiPfad;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    private Standort standort;

    @ManyToOne
    @JoinColumn(name = "benutzer_id")
    private Benutzer erstelltVon;
}
