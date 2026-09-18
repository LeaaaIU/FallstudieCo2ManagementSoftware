package de.co2management.backend.controller;

import de.co2management.backend.dto.StandortRequest;
import de.co2management.backend.dto.StandortResponse;
import de.co2management.backend.service.StandortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standorte")
@RequiredArgsConstructor
public class StandortController {

    private final StandortService standortService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<StandortResponse> getStandorte() {
        return standortService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public StandortResponse getStandortById(@PathVariable Long id) {
        return standortService.findResponseById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<StandortResponse> createStandort(@Valid @RequestBody StandortRequest req) {
        return ResponseEntity.status(201).body(standortService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<StandortResponse> updateStandort(@PathVariable Long id,
                                                             @Valid @RequestBody StandortRequest req) {
        return ResponseEntity.ok(standortService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteStandort(@PathVariable Long id) {
        standortService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}