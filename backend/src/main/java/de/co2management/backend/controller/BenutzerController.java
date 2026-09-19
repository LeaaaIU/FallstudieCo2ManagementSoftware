package de.co2management.backend.controller;

import de.co2management.backend.dto.BenutzerRequest;
import de.co2management.backend.dto.BenutzerResponse;
import de.co2management.backend.service.BenutzerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/benutzer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class BenutzerController {

    private final BenutzerService benutzerService;

    @GetMapping
    public List<BenutzerResponse> getBenutzer() {
        return benutzerService.findAll();
    }

    @GetMapping("/{id}")
    public BenutzerResponse getBenutzerById(@PathVariable Long id) {
        return benutzerService.findResponseById(id);
    }

    @PostMapping
    public ResponseEntity<BenutzerResponse> createBenutzer(@Valid @RequestBody BenutzerRequest req) {
        return ResponseEntity.status(201).body(benutzerService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BenutzerResponse> updateBenutzer(@PathVariable Long id,
                                                             @Valid @RequestBody BenutzerRequest req) {
        return ResponseEntity.ok(benutzerService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBenutzer(@PathVariable Long id) {
        benutzerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}