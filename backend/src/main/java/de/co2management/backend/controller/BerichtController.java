package de.co2management.backend.controller;

import de.co2management.backend.dto.BerichtRequest;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.entity.Bericht;
import de.co2management.backend.enums.RolleEnum;
import de.co2management.backend.repository.BenutzerRepository;
import de.co2management.backend.service.BerichtService;
import de.co2management.backend.service.PdfExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/berichte")
@RequiredArgsConstructor
public class BerichtController {

    private final BerichtService berichtService;
    private final PdfExportService pdfExportService;
    private final BenutzerRepository benutzerRepo;

    @PostMapping
    public ResponseEntity<Bericht> createBericht(@Valid @RequestBody BerichtRequest req,
                                                  Authentication authentication) {
        return ResponseEntity.status(201)
            .body(berichtService.createBericht(req, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bericht> getBericht(@PathVariable Long id, Authentication authentication) {
        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return ResponseEntity.ok(berichtService.getBericht(id, aktuellerBenutzer));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getBerichtPdf(@PathVariable Long id, Authentication authentication) {
        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .body(berichtService.generatePdf(id, aktuellerBenutzer));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportEmissionenPdf(
            @RequestParam(required = false) Long standortId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            Authentication authentication) {

        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        Long effektiverStandortId = ermittleEffektivenStandort(standortId, aktuellerBenutzer);

        byte[] pdf = pdfExportService.exportEmissionsPdf(effektiverStandortId, von, bis);
        String dateiname = "co2-bericht_" + von + "_" + bis + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dateiname + "\"")
            .body(pdf);
    }

    @GetMapping
    public List<Bericht> getBerichte(Authentication authentication) {
        Benutzer aktuellerBenutzer = ermittleAktuellenBenutzer(authentication);
        return berichtService.findAll(aktuellerBenutzer);
    }

    private Benutzer ermittleAktuellenBenutzer(Authentication authentication) {
        return benutzerRepo.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
    }

    private Long ermittleEffektivenStandort(Long angefragterStandortId, Benutzer aktuellerBenutzer) {
        if (aktuellerBenutzer.getRolle() == RolleEnum.BENUTZER) {
            return aktuellerBenutzer.getStandort().getId();
        }
        return angefragterStandortId;
    }
}