package de.co2management.backend.controller;

import de.co2management.backend.dto.AuthMeResponse;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.repository.BenutzerRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final BenutzerRepository benutzerRepository; 

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpSession session) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.passwort())
            );
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc
            );
            return ResponseEntity.ok(Map.of("username", auth.getName()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Ungültige Zugangsdaten"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
public ResponseEntity<?> me(Authentication authentication) {
    if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Nicht eingeloggt"));
    }

    String username = authentication.getName();

    Benutzer benutzer = benutzerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException(
                    "Eingeloggter Benutzer '" + username + "' nicht in der Datenbank gefunden"));

    AuthMeResponse response = new AuthMeResponse(
            benutzer.getUsername(),
            benutzer.getRolle().name(),
            benutzer.getStandort() != null ? benutzer.getStandort().getId() : null,
            benutzer.getStandort() != null ? benutzer.getStandort().getName() : null
    );

    return ResponseEntity.ok(response);
}

    public record LoginRequest(String username, String passwort) {}
}