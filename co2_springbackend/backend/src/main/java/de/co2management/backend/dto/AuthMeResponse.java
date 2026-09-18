package de.co2management.backend.dto;

public record AuthMeResponse(
        String username,
        String rolle,
        Long standortId,
        String standortName
) {}