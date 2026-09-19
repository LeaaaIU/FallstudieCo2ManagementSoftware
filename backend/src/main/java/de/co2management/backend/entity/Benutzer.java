package de.co2management.backend.entity;

import de.co2management.backend.enums.RolleEnum;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "benutzer")
@Data
public class Benutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String passwort;

    @Enumerated(EnumType.STRING)
    private RolleEnum rolle;

    private boolean aktiv;
    private LocalDateTime erstelltAm;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    private Standort standort;
}