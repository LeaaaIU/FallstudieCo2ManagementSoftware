package de.co2management.backend.dto;

import java.time.LocalDateTime;

public record StandortResponse(
        Long id,
        String name,
        String adresse,
        LocalDateTime erstelltAm
) {}