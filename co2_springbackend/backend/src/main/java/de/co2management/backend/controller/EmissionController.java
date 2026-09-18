package de.co2management.backend.controller;

import de.co2management.backend.dto.EmissionRequest;
import de.co2management.backend.dto.StandortStatistikResponse;
import de.co2management.backend.dto.ZeitreihenPunkt;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.entity.EmissionEintrag;
import de.co2management.backend.repository.BenutzerRepository;
import de.co2management.backend.service.EmissionService;
import de.co2management.backend.service.StandortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/emissionen")
@RequiredArgsConstructor
public class EmissionController {

    private final EmissionService emissionService;
    private final StandortService standortService;
    private final BenutzerRepository benutzerRepo;

    @GetMapping
    public List<EmissionEintrag> getEmissionen(
            @RequestParam(required = false) Long standortId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Authentication authentication) {

        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return emissionService.findGefiltert(standortId, von, bis, aktuellerBenutzer);
    }

    @GetMapping("/statistik/standort")
    public List<StandortStatistikResponse> getStatistikProStandort(Authentication authentication) {
        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return emissionService.statistikProStandort(aktuellerBenutzer);
    }

    @GetMapping("/statistik/zeitreihe")
    public List<ZeitreihenPunkt> getZeitreihe(
            @RequestParam(required = false) Long standortId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Authentication authentication) {

        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return emissionService.zeitreihe(standortId, von, bis, aktuellerBenutzer);
    }

    @PostMapping
    public ResponseEntity<EmissionEintrag> createEmission(
            @Valid @RequestBody EmissionRequest req,
            Authentication authentication) {

        Benutzer erfasser = ermittleAktuellenBenutzer(authentication);

        EmissionEintrag entry = new EmissionEintrag();
        entry.setKategorie(req.kategorie());
        entry.setWertCo2Kg(req.wertCo2Kg());
        entry.setDatum(req.datum());
        entry.setBeschreibung(req.beschreibung());
        entry.setErfasstAm(LocalDateTime.now());
        entry.setStandort(standortService.findById(req.standortId()));
        entry.setErfasstVon(erfasser);

        return ResponseEntity.status(201).body(emissionService.save(entry));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmissionEintrag> updateEmission(
            @PathVariable Long id,
            @Valid @RequestBody EmissionRequest req) {

        EmissionEintrag entry = emissionService.findById(id);
        entry.setKategorie(req.kategorie());
        entry.setWertCo2Kg(req.wertCo2Kg());
        entry.setDatum(req.datum());
        entry.setBeschreibung(req.beschreibung());
        return ResponseEntity.ok(emissionService.save(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmission(@PathVariable Long id) {
        emissionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Benutzer ermittleAktuellenBenutzer(Authentication authentication) {
        return benutzerRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
    }
}