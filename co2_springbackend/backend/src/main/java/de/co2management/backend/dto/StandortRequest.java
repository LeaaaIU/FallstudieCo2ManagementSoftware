package de.co2management.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record StandortRequest(
        @NotBlank(message = "Name darf nicht leer sein")
        String name,

        @NotBlank(message = "Adresse darf nicht leer sein")
        String adresse
) {}