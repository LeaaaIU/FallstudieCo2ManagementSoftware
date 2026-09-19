package de.co2management.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BerichtRequest(
        @NotNull(message = "Zeitraum-Von muss angegeben werden")
        LocalDate zeitraumVon,

        @NotNull(message = "Zeitraum-Bis muss angegeben werden")
        LocalDate zeitraumBis,

        @NotNull(message = "Standort muss angegeben werden")
        Long standortId
) {}