package de.co2management.backend.dto;

import de.co2management.backend.enums.RolleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BenutzerRequest(
        @NotBlank(message = "Benutzername darf nicht leer sein")
        String username,

        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "E-Mail muss gültig sein")
        String email,

        String passwort,

        @NotNull(message = "Rolle muss angegeben werden")
        RolleEnum rolle,

        @NotNull(message = "Standort muss angegeben werden")
        Long standortId,

        Boolean aktiv
) {}