package de.co2management.backend.dto;

import de.co2management.backend.enums.RolleEnum;

import java.time.LocalDateTime;

public record BenutzerResponse(
        Long id,
        String username,
        String email,
        RolleEnum rolle,
        Long standortId,
        String standortName,
        boolean aktiv,
        LocalDateTime erstelltAm
) {}