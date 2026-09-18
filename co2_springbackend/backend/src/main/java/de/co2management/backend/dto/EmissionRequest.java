package de.co2management.backend.dto;

import de.co2management.backend.enums.KategorieEnum;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EmissionRequest(
    @NotNull KategorieEnum kategorie,
    @NotNull @Positive Double wertCo2Kg,
    @NotNull LocalDate datum,
    String beschreibung,
    @NotNull Long standortId
) {}